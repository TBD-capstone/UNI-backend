package uni.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uni.backend.domain.Matching;

import java.util.List;
import java.util.Optional;

public interface MatchingRepository extends JpaRepository<Matching, Integer> {

    List<Matching> findByRequester_UserId(Integer requesterId);

    List<Matching> findByReceiver_UserId(Integer receiverId);

    Optional<Matching> findByRequester_UserIdAndReceiver_UserIdAndStatus(Integer requesterId, Integer receiverId, Matching.Status status);

}
