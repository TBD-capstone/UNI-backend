package uni.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uni.backend.domain.Qna;
import uni.backend.domain.QnaLikes;
import uni.backend.domain.User;
import uni.backend.repository.QnaLikeRepository;
import uni.backend.repository.QnaRepository;
import uni.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class QnaServiceTest {


    @Mock // 실제 DB를 사용하지 않기 위해서 사용하는 어노테이션
    private QnaRepository qnaRepository;

    @Mock
    private UserRepository userRepository;
    @Mock
    private QnaLikeRepository qnaLikeRepository;

    @InjectMocks // mock된 QnaRepository, UserRepository를 주입
    private QnaService qnaService;

    @Test
    public void Qna_작성_테스트() {
        // Given
        Integer userId = 1;
        Integer commenterId = 2;
        String content = "테스트 Qna 작성입니다.";

        User user = new User();
        user.setUserId(userId);
        User commenter = new User();
        commenter.setUserId(commenterId);

        Qna newQna = new Qna();
        newQna.setProfileOwner(user);
        newQna.setCommenter(commenter);
        newQna.setContent(content);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(commenterId))
            .thenReturn(
                Optional.of(
                    commenter));  //userRepository.findById(commenterId) 호출 시 commenter 객체 반환
        when(qnaRepository.save(any(Qna.class))).thenReturn(newQna);
        //QnA 저장 시 반환되는 객체를 지정

        // When
        Qna createdQna = qnaService.createQna(userId, commenterId, content);

        // Then
        assertNotNull(createdQna);
        assertEquals(userId, createdQna.getProfileOwner().getUserId()); // 생성된 qna에 해당하는 프로필의 주인 검증
        assertEquals(commenterId, createdQna.getCommenter().getUserId()); //qna 작성자 아이디 검증
        assertEquals(content, createdQna.getContent()); //내용 검증
    }

    @Test
    public void Qna_삭제_테스트() {
        // Given
        Integer qnaId = 1;
        Qna qna = new Qna();
        qna.setQnaId(qnaId);

        when(qnaRepository.findById(qnaId)).thenReturn(Optional.of(qna));

        // When
        Qna deletedQna = qnaService.deleteQna(qnaId);

        // Then
        assertTrue(deletedQna.getDeleted()); // 삭제된 QnA는 deleted가 true여야 한다.
    }


    @Test
    public void 좋아요_증가_테스트() {
        // Given
        Integer qnaId = 1;
        User user = new User();
        user.setUserId(1); // 사용자 ID 설정
        Qna qna = new Qna();
        qna.setQnaId(qnaId);
        qna.setLikes(0L); // 초기 좋아요 수

        // Mockito 설정
        Mockito.when(qnaRepository.findById(qnaId)).thenReturn(Optional.of(qna));
        Mockito.when(qnaLikeRepository.findByUserAndQna(user, qna))
            .thenReturn(Optional.empty()); // 좋아요가 없다고 설정
        Mockito.when(qnaRepository.save(Mockito.any(Qna.class)))
            .thenAnswer(invocation -> invocation.getArgument(0)); // 저장 시 반환된 Qna 객체 설정

        // When
        Qna updatedQna = qnaService.toggleLike(qnaId, user);

        // Then
        assertNotNull(updatedQna); // updatedQna가 null이 아니어야 합니다.
        assertEquals(1L, updatedQna.getLikes()); // 좋아요가 증가했는지 확인
    }

    @Test
    public void 좋아요_감소_테스트() {
        // Given
        Integer qnaId = 1;
        User user = new User();
        user.setUserId(1); // 사용자 ID 설정

        // 이미 좋아요가 있는 상태에서 테스트
        Qna qna = new Qna();
        qna.setQnaId(qnaId);
        qna.setLikes(1L); // 좋아요가 1로 시작

        // Mockito 설정
        Mockito.when(qnaRepository.findById(qnaId)).thenReturn(Optional.of(qna));
        Mockito.when(qnaLikeRepository.findByUserAndQna(user, qna))
            .thenReturn(Optional.of(new QnaLikes())); // 이미 좋아요를 눌렀다고 설정
        Mockito.when(qnaRepository.save(Mockito.any(Qna.class)))
            .thenAnswer(invocation -> invocation.getArgument(0)); // 저장 시 반환된 Qna 객체 설정

        // When
        Qna updatedQna = qnaService.toggleLike(qnaId, user);

        // Then
        assertNotNull(updatedQna); // updatedQna가 null이 아니어야 합니다.
        assertEquals(0L, updatedQna.getLikes()); // 좋아요가 취소되었는지 확인
    }


}
