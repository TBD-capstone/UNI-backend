package uni.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uni.backend.domain.Matching;

public interface MatchingRepository extends JpaRepository<Matching, Integer> {
}
