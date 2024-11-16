package uni.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uni.backend.domain.Qna;
import uni.backend.domain.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {


    List<Review> findByProfileOwnerUserId(Integer userId);
}
