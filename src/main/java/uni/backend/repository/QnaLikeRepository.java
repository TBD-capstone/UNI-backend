package uni.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uni.backend.domain.Qna;
import uni.backend.domain.QnaLikes;
import uni.backend.domain.User;

@Repository
public interface QnaLikeRepository extends JpaRepository<QnaLikes, Integer> {

    // 특정 사용자와 Qna에 대한 좋아요 확인
    Optional<QnaLikes> findByUserAndQna(User user, Qna qna);
}