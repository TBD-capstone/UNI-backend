package uni.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uni.backend.domain.Qna;
import uni.backend.domain.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // 특정 유저의 모든 리뷰 조회
    List<Review> findByProfileOwnerUserId(Integer userId);

    // 특정 유저의 블라인드 리뷰 조회
    List<Review> findByCommenter_UserId(Integer userId);


}
