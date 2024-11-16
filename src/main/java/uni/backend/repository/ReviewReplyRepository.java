package uni.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uni.backend.domain.ReviewReply;

import java.util.List;

public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Integer> {

    List<ReviewReply> findByReview_ReviewId(Integer reviewId);
}