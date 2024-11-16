package uni.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uni.backend.domain.ReviewReply;
import uni.backend.domain.ReviewReplyLikes;
import uni.backend.domain.User;

import java.util.Optional;

@Repository
public interface ReviewReplyLikeRepository extends JpaRepository<ReviewReplyLikes, Integer> {

    // 특정 사용자와 대댓글 간의 좋아요 상태를 확인
    Optional<ReviewReplyLikes> findByUserAndReviewReply(User user, ReviewReply reviewReply);

}
