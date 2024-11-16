package uni.backend.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Review;
import uni.backend.domain.ReviewReply;
import uni.backend.domain.ReviewReplyLikes;
import uni.backend.domain.User;
import uni.backend.repository.ReviewReplyLikeRepository;
import uni.backend.repository.ReviewReplyRepository;
import uni.backend.repository.ReviewRepository;
import uni.backend.repository.UserRepository;

@Service
public class ReviewReplyService {

    private final ReviewReplyRepository reviewReplyRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewReplyLikeRepository reviewReplyLikeRepository;

    public ReviewReplyService(ReviewReplyRepository reviewReplyRepository,
        ReviewRepository reviewRepository,
        UserRepository userRepository,
        ReviewReplyLikeRepository reviewReplyLikeRepository) {
        this.reviewReplyRepository = reviewReplyRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.reviewReplyLikeRepository = reviewReplyLikeRepository;
    }

    // **대댓글 작성**
    @Transactional
    public ReviewReply createReply(Integer reviewId, Integer commenterId, String content) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. ID: " + reviewId));

        User commenter = userRepository.findById(commenterId)
            .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다. ID: " + commenterId));

        ReviewReply reply = new ReviewReply(review, commenter, content);
        return reviewReplyRepository.save(reply); // 저장 후 반환
    }

    // **대댓글 삭제**
    @Transactional
    public ReviewReply deleteReply(Integer replyId) {
        ReviewReply reply = reviewReplyRepository.findById(replyId)
            .orElseThrow(() -> new IllegalArgumentException("대댓글을 찾을 수 없습니다. ID: " + replyId));

        reply.softDelete(); // 소프트 삭제
        return reviewReplyRepository.save(reply); // 상태 변경 후 저장
    }

    // **대댓글 수정**
    @Transactional
    public void updateReplyContent(Integer replyId, String newContent) {
        ReviewReply reply = reviewReplyRepository.findById(replyId)
            .orElseThrow(() -> new IllegalArgumentException("대댓글을 찾을 수 없습니다. ID: " + replyId));

        if (reply.getDeleted() != null && reply.getDeleted()) {
            throw new IllegalStateException("삭제된 대댓글은 수정할 수 없습니다.");
        }

        reply.updateContent(newContent); // 수정 내용과 수정 시간 업데이트
        reviewReplyRepository.save(reply); // 변경 사항 저장
    }


    // **특정 대댓글 조회**
    @Transactional(readOnly = true)
    public ReviewReply getReply(Integer replyId) {
        return reviewReplyRepository.findById(replyId)
            .orElseThrow(() -> new IllegalArgumentException("대댓글을 찾을 수 없습니다. ID: " + replyId));
    }

    // **특정 리뷰의 모든 대댓글 조회**
    @Transactional(readOnly = true)
    public List<ReviewReply> getRepliesByReviewId(Integer reviewId) {
        return reviewReplyRepository.findByReview_ReviewId(reviewId);
    }

    // **대댓글 좋아요 토글**
    @Transactional
    public void toggleLike(Integer replyId, User user) {
        ReviewReply reply = reviewReplyRepository.findById(replyId)
            .orElseThrow(() -> new IllegalArgumentException("대댓글을 찾을 수 없습니다. ID: " + replyId));

        Optional<ReviewReplyLikes> existingLike = reviewReplyLikeRepository.findByUserAndReviewReply(
            user,
            reply);

        if (existingLike.isPresent()) {
            // 좋아요 취소
            reviewReplyLikeRepository.delete(existingLike.get());
            reply.decreaseLikes(); // 좋아요 수 감소
        } else {
            // 좋아요 추가
            ReviewReplyLikes like = new ReviewReplyLikes();
            like.setUser(user);
            like.setReviewReply(reply);
            ;
            reviewReplyLikeRepository.save(like);
            reply.increaseLikes(); // 좋아요 수 증가
        }

        reviewReplyRepository.save(reply); // 업데이트된 대댓글 저장
    }
}
