package uni.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uni.backend.domain.Profile;
import uni.backend.domain.Qna;
import uni.backend.domain.QnaLikes;
import uni.backend.domain.Reply;
import uni.backend.domain.User;
import uni.backend.domain.dto.QnaResponse;
import uni.backend.repository.QnaLikeRepository;
import uni.backend.repository.QnaRepository;
import uni.backend.repository.UserRepository;

public class QnaServiceTest {

    @Mock
    private QnaRepository qnaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QnaLikeRepository qnaLikeRepository;

    @InjectMocks
    private QnaService qnaService;

    private User profileOwner;
    private User commenter;
    private Qna qna;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        profileOwner = new User();
        profileOwner.setUserId(1);
        profileOwner.setName("Profile Owner");
        Profile ownerProfile = new Profile();
        ownerProfile.setImgProf("owner/image.jpg");
        profileOwner.setProfile(ownerProfile);

        commenter = new User();
        commenter.setUserId(2);
        commenter.setName("Commenter");
        Profile commenterProfile = new Profile();
        commenterProfile.setImgProf("commenter/image.jpg");
        commenter.setProfile(commenterProfile);

        qna = new Qna();
        qna.setQnaId(1);
        qna.setProfileOwner(profileOwner);
        qna.setCommenter(commenter);
        qna.setLikes(10L);
        qna.setBlind(false);
        qna.setDeleted(false);

        // Reply 추가
        Reply reply = new Reply();
        reply.setReplyId(100);
        reply.setQna(qna);
        reply.setCommenter(commenter);
        reply.setIsBlind(false);
        reply.setDeleted(false);
        qna.setReplies(List.of(reply)); // Qna에 Replies 설정
    }


    @Test
    void Qna_생성_성공() {
        when(userRepository.findById(1)).thenReturn(Optional.of(profileOwner));
        when(userRepository.findById(2)).thenReturn(Optional.of(commenter));
        when(qnaRepository.save(any(Qna.class))).thenReturn(qna);

        QnaResponse response = qnaService.createQna(1, 2, "새 QnA 내용");

        assertNotNull(response);
        assertEquals(1, response.getProfileOwner().getUserId());
        assertEquals("새 QnA 내용", response.getContent());
    }

    @Test
    void 좋아요_추가_성공() {
        // given
        when(qnaRepository.findById(qna.getQnaId())).thenReturn(Optional.of(qna));
        when(qnaLikeRepository.findByUserAndQna(commenter, qna)).thenReturn(Optional.empty());

        // when
        qnaService.toggleLike(qna.getQnaId(), commenter);

        // then
        assertEquals(11L, qna.getLikes());  // 좋아요 수가 2로 증가해야 한다
        verify(qnaLikeRepository).save(any(QnaLikes.class));  // 좋아요 저장
        verify(qnaRepository).save(qna);  // Qna 업데이트
    }

    @Test
    void 좋아요_취소_성공() {
        // given
        QnaLikes like = new QnaLikes();
        like.setUser(commenter);
        like.setQna(qna);

        when(qnaRepository.findById(qna.getQnaId())).thenReturn(Optional.of(qna));
        when(qnaLikeRepository.findByUserAndQna(commenter, qna)).thenReturn(Optional.of(like));

        // when
        qnaService.toggleLike(qna.getQnaId(), commenter);

        // then
        assertEquals(9L, qna.getLikes());  // 좋아요 수가 0으로 감소해야 한다
        verify(qnaLikeRepository).delete(like);  // 좋아요 삭제
        verify(qnaRepository).save(qna);  // Qna 업데이트
    }

    @Test
    void Qna_삭제() {
        when(qnaRepository.findById(1)).thenReturn(Optional.of(qna));

        Qna deletedQna = qnaService.deleteQna(1);

        assertTrue(deletedQna.getDeleted());
    }

    @Test
    void Qna_목록_조회() {
        when(qnaRepository.findByProfileOwnerUserId(1)).thenReturn(List.of(qna));

        List<QnaResponse> responses = qnaService.getUserQnas(1);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1, responses.get(0).getQnaId());
    }

    @Test
    void Qna_삭제된_Qna_조회() {
        qna.setDeleted(true);
        when(qnaRepository.findByProfileOwnerUserId(1)).thenReturn(List.of(qna));

        List<QnaResponse> responses = qnaService.getUserQnas(1);

        assertTrue(responses.get(0).getDeleted());
    }
}
