package uni.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uni.backend.domain.Hashtag;

import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    Optional<Hashtag> findByHashtagName(String hashtagName);
}
