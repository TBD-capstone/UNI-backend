package uni.backend.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Qna;
import uni.backend.domain.Reply;
import uni.backend.domain.ReplyLikes;
import uni.backend.domain.User;
import uni.backend.domain.dto.ReplyResponse;
import uni.backend.repository.QnaRepository;
import uni.backend.repository.ReplyLikeRepository;
import uni.backend.repository.ReplyRepository;
import uni.backend.repository.UserRepository;

@Service
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final QnaRepository qnaRepository;
    private final UserRepository userRepository;
    private final ReplyLikeRepository replyLikeRepository;

    public ReplyService(ReplyRepository replyRepository, QnaRepository qnaRepository,
        UserRepository userRepository, ReplyLikeRepository replyLikeRepository) {
        this.replyRepository = replyRepository;
        this.qnaRepository = qnaRepository;
        this.userRepository = userRepository;
        this.replyLikeRepository = replyLikeRepository;
    }

    private ReplyResponse createReplyResponse(Reply reply) {
        return new ReplyResponse(
            reply.getReplyId(),
            reply.getCommenter().getUserId(),
            reply.getCommenter().getName(),
            reply.getBlindReply(), // 블라인드 여부 반영
            reply.getQna().getQnaId(),
            reply.getCommenter().getProfile().getImgProf(),
            reply.getDeleted(),
            reply.getDeleted() ? "삭제된 대댓글입니다." : null,
            reply.getLikes()
        );
    }

    @Transactional
    public ReplyResponse createReply(Integer qnaId, Integer commenterId, String content) {
        Qna qna = qnaRepository.findById(qnaId)
            .orElseThrow(() -> new IllegalArgumentException("QnA를 찾을 수 없습니다. ID: " + qnaId));
        User commenter = userRepository.findById(commenterId)
            .orElseThrow(
                () -> new IllegalArgumentException("댓글 작성자를 찾을 수 없습니다. ID: " + commenterId));

        Reply reply = new Reply(qna, commenter, content);
        replyRepository.save(reply);

        return createReplyResponse(reply);
    }

    public List<ReplyResponse> getRepliesByQnaId(Integer qnaId) {
        List<Reply> replies = replyRepository.findByQna_QnaId(qnaId);
        return replies.stream()
            .map(this::createReplyResponse)
            .toList();
    }

    @Transactional
    public Reply deleteReply(Integer replyId) {
        Reply reply = replyRepository.findById(replyId)
            .orElseThrow(() -> new IllegalArgumentException("대댓글을 찾을 수 없습니다. ID: " + replyId));
        reply.softDelete();
        return replyRepository.save(reply);
    }

    @Transactional
    public Reply updateReplyContent(Integer replyId, String newContent) {
        Reply reply = replyRepository.findById(replyId)
            .orElseThrow(() -> new IllegalArgumentException("대댓글을 찾을 수 없습니다. ID: " + replyId));
        reply.updateContent(newContent);
        return replyRepository.save(reply);
    }

    @Transactional
    public void toggleLike(Integer replyId, User user) {
        Reply reply = replyRepository.findById(replyId)
            .orElseThrow(() -> new IllegalArgumentException("대댓글을 찾을 수 없습니다. ID: " + replyId));
        Optional<ReplyLikes> existingLike = replyLikeRepository.findByUserAndReply(user, reply);

        if (existingLike.isPresent()) {
            replyLikeRepository.delete(existingLike.get());
            reply.decreaseLikes();
        } else {
            ReplyLikes like = new ReplyLikes();
            like.setUser(user);
            like.setReply(reply);
            replyLikeRepository.save(like);
            reply.increaseLikes();
        }

        replyRepository.save(reply);
    }

    @Transactional(readOnly = true)
    public Reply getReply(Integer replyId) {
        return replyRepository.findById(replyId)
            .orElseThrow(() -> new IllegalArgumentException("대댓글을 찾을 수 없습니다. ID: " + replyId));
    }
}
