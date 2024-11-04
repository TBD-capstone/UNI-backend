package uni.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uni.backend.domain.Hashtag;

import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByHashtagName(String hashtagName);

    Optional<Hashtag> findByHashtagId(Integer hashtagId); // 이 메서드 추가
}
