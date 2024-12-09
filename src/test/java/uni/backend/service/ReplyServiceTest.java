package uni.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uni.backend.domain.Profile;
import uni.backend.domain.Qna;
import uni.backend.domain.Reply;
import uni.backend.domain.ReplyLikes;
import uni.backend.domain.User;
import uni.backend.domain.dto.ReplyResponse;
import uni.backend.repository.QnaRepository;
import uni.backend.repository.ReplyLikeRepository;
import uni.backend.repository.ReplyRepository;
import uni.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ReplyServiceTest {

    @Mock
    private ReplyRepository replyRepository;

    @Mock
    private QnaRepository qnaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReplyLikeRepository replyLikeRepository;

    @InjectMocks
    private ReplyService replyService;

    @Test
    void 대댓글_작성_성공() {
        // Given
        Integer qnaId = 1;
        Integer commenterId = 2;
        String content = "Test reply content.";

        Qna qna = new Qna();
        qna.setQnaId(qnaId);

        User commenter = new User();
        commenter.setUserId(commenterId);

        Profile profile = new Profile();
        profile.setImgProf("example.jpg");
        commenter.setProfile(profile);

        Reply reply = new Reply(qna, commenter, content);

        when(qnaRepository.findById(qnaId)).thenReturn(Optional.of(qna));
        when(userRepository.findById(commenterId)).thenReturn(Optional.of(commenter));
        when(replyRepository.save(any(Reply.class))).thenReturn(reply);

        // When
        ReplyResponse createdReply = replyService.createReply(qnaId, commenterId, content);

        // Then
        assertNotNull(createdReply);
        assertEquals(content, createdReply.getContent());
        assertEquals(commenterId, createdReply.getCommenterId());
        verify(replyRepository).save(any(Reply.class));
    }

    @Test
    void 대댓글_작성_실패_QnA_없음() {
        // Given
        Integer qnaId = 1;
        Integer commenterId = 2;
        String content = "Test reply content.";

        when(qnaRepository.findById(qnaId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> replyService.createReply(qnaId, commenterId, content));
        assertEquals("QnA를 찾을 수 없습니다. ID: " + qnaId, exception.getMessage());
    }

    @Test
    void 좋아요_증가_성공() {
        // Given
        Integer replyId = 1;
        User user = new User();
        user.setUserId(1);
        Reply reply = new Reply();
        reply.setReplyId(replyId);
        reply.setLikes(0L);

        when(replyRepository.findById(replyId)).thenReturn(Optional.of(reply));
        when(replyLikeRepository.findByUserAndReply(user, reply)).thenReturn(Optional.empty());

        // When
        replyService.toggleLike(replyId, user);

        // Then
        assertEquals(1L, reply.getLikes());
        verify(replyLikeRepository).save(any(ReplyLikes.class));
        verify(replyRepository).save(reply);
    }

    @Test
    void 좋아요_감소_성공() {
        // Given
        Integer replyId = 1;
        User user = new User();
        user.setUserId(1);
        Reply reply = new Reply();
        reply.setReplyId(replyId);
        reply.setLikes(1L);

        ReplyLikes like = new ReplyLikes();
        like.setUser(user);
        like.setReply(reply);

        when(replyRepository.findById(replyId)).thenReturn(Optional.of(reply));
        when(replyLikeRepository.findByUserAndReply(user, reply)).thenReturn(Optional.of(like));

        // When
        replyService.toggleLike(replyId, user);

        // Then
        assertEquals(0L, reply.getLikes());
        verify(replyLikeRepository).delete(like);
        verify(replyRepository).save(reply);
    }

    @Test
    void 대댓글_삭제_성공() {
        // Given
        Integer replyId = 1;
        Reply reply = new Reply();
        reply.setReplyId(replyId);
        reply.setDeleted(false); // 초기 상태에서 삭제되지 않음

        Mockito.when(replyRepository.findById(replyId)).thenReturn(Optional.of(reply));
        Mockito.when(replyRepository.save(reply)).thenReturn(reply); // 삭제 후 상태를 저장하도록 모킹

        // When
        Reply deletedReply = replyService.deleteReply(replyId);

        // Then
        assertNotNull(deletedReply);
        assertTrue(deletedReply.getDeleted());
        verify(replyRepository).save(reply);
    }


    @Test
    void 대댓글_삭제_실패() {
        // Given
        Integer replyId = 1;

        when(replyRepository.findById(replyId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> replyService.deleteReply(replyId));
        assertEquals("대댓글을 찾을 수 없습니다. ID: " + replyId, exception.getMessage());
    }

    @Test
    void 대댓글_내용_수정_성공() {
        // Given
        Integer replyId = 1;
        String newContent = "Updated content";

        Reply reply = new Reply();
        reply.setReplyId(replyId);
        reply.setContent("Old content");

        when(replyRepository.findById(replyId)).thenReturn(Optional.of(reply));
        when(replyRepository.save(any(Reply.class))).thenReturn(reply);

        // When
        Reply updatedReply = replyService.updateReplyContent(replyId, newContent);

        // Then
        assertNotNull(updatedReply);
        assertEquals(newContent, updatedReply.getContent());
        verify(replyRepository).save(reply);
    }

    @Test
    void 대댓글_내용_수정_실패() {
        // Given
        Integer replyId = 1;
        String newContent = "Updated content";

        when(replyRepository.findById(replyId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> replyService.updateReplyContent(replyId, newContent));
        assertEquals("대댓글을 찾을 수 없습니다. ID: " + replyId, exception.getMessage());
    }

    @Test
    void 대댓글_조회_성공() {
        // Given
        Integer replyId = 1;
        Reply reply = new Reply();
        reply.setReplyId(replyId);

        when(replyRepository.findById(replyId)).thenReturn(Optional.of(reply));

        // When
        Reply fetchedReply = replyService.getReply(replyId);

        // Then
        assertNotNull(fetchedReply);
        assertEquals(replyId, fetchedReply.getReplyId());
    }

    @Test
    void 대댓글_조회_실패() {
        // Given
        Integer replyId = 1;

        when(replyRepository.findById(replyId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> replyService.getReply(replyId));
        assertEquals("대댓글을 찾을 수 없습니다. ID: " + replyId, exception.getMessage());
    }
}
