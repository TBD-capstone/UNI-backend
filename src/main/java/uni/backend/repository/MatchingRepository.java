package uni.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uni.backend.domain.Matching;

import java.util.List;

public interface MatchingRepository extends JpaRepository<Matching, Integer> {
    List<Matching> findByRequester_UserId(Integer requesterId);
    List<Matching> findByReceiver_UserId(Integer receiverId);
}
