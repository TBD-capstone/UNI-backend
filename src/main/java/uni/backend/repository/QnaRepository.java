package uni.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uni.backend.domain.Profile;
import uni.backend.domain.Qna;

@Repository
public interface QnaRepository extends JpaRepository<Qna, Integer> {

  List<Qna> findByProfileOwnerUserId(Integer userId);
}
