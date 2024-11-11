package uni.backend.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uni.backend.domain.Qna;
import uni.backend.domain.Reply;
import uni.backend.domain.ReplyLikes;
import uni.backend.domain.User;
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

    // 새로운 대댓글 작성
    @Test
    public void 대댓글_작성() {
        // Given
        Integer qnaId = 1;
        Integer commenterId = 2;
        String content = "This is a test reply.";

        Qna qna = new Qna();
        qna.setQnaId(qnaId);

        User commenter = new User();
        commenter.setUserId(commenterId);

        Reply newReply = new Reply(qna, commenter, content);

        Mockito.when(qnaRepository.findById(qnaId)).thenReturn(Optional.of(qna));
        Mockito.when(userRepository.findById(commenterId)).thenReturn(Optional.of(commenter));
        Mockito.when(replyRepository.save(Mockito.any(Reply.class))).thenReturn(newReply);

        // When
        Reply createdReply = replyService.createReply(qnaId, commenterId, content);

        // Then
        assertNotNull(createdReply);
        assertEquals(content, createdReply.getContent());
        assertEquals(commenterId, createdReply.getCommenter().getUserId());
    }

    // 대댓글 좋아요 추가
    @Test
    public void 좋아요_증가() {
        // Given
        Integer replyId = 1;
        User user = new User();
        user.setUserId(1);
        Reply reply = new Reply();
        reply.setReplyId(replyId);
        reply.setLikes(0L); // 초기 좋아요 수

        Mockito.when(replyRepository.findById(replyId)).thenReturn(Optional.of(reply));
        Mockito.when(replyLikeRepository.findByUserAndReply(user, reply))
            .thenReturn(Optional.empty()); // 좋아요가 없다고 설정

        // When
        replyService.toggleLike(replyId, user);

        // Then
        assertEquals(1L, reply.getLikes()); // 좋아요가 1로 증가해야 함
    }

    // 대댓글 좋아요 취소
    @Test
    public void 좋아요_감소() {
        // Given
        Integer replyId = 1;
        User user = new User();
        user.setUserId(1);
        Reply reply = new Reply();
        reply.setReplyId(replyId);
        reply.setLikes(1L); // 좋아요가 1로 시작

        Mockito.when(replyRepository.findById(replyId)).thenReturn(Optional.of(reply));
        Mockito.when(replyLikeRepository.findByUserAndReply(user, reply))
            .thenReturn(Optional.of(new ReplyLikes())); // 이미 좋아요가 눌려짐

        // When
        replyService.toggleLike(replyId, user);

        // Then
        assertEquals(0L, reply.getLikes()); // 좋아요가 0으로 취소되어야 함
    }

    // 대댓글 삭제
    @Test
    public void 대댓글_삭제() {
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
        assertNotNull(deletedReply); // deletedReply가 null이 아니어야 함
        assertTrue(deletedReply.getDeleted()); // 삭제된 상태가 true인지 확인
    }

}
