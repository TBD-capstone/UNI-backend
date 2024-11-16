package uni.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uni.backend.domain.Review;
import uni.backend.domain.ReviewLikes;
import uni.backend.domain.User;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLikes, Integer> {

    // 특정 사용자와 리뷰에 대한 좋아요 확인
    Optional<ReviewLikes> findByUserAndReview(User user, Review review);
}