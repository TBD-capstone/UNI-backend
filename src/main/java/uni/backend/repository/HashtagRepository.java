package uni.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uni.backend.domain.Hashtag;

import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByHashtagName(String hashtagName);

    Optional<Hashtag> findByHashtagId(Integer hashtagId);

    Optional<Hashtag> findByEnName(String entagName); // 영어 이름으로 검색

    Optional<Hashtag> findByZhName(String zhtagName); // 중국어 이름으로 검색
}
