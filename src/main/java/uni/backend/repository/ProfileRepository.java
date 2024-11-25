package uni.backend.repository;

import aj.org.objectweb.asm.commons.Remapper;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uni.backend.domain.Profile;
import uni.backend.domain.Role;
import uni.backend.domain.User;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Integer> { // ID 타입을 Integer로 수정

    @EntityGraph(attributePaths = {"mainCategories.hashtag"})
        // 해시태그를 포함하여 프로필 조회
    Optional<Profile> findByUser_UserId(Integer userId);

    Page<Profile> findByUser_UserId(Integer userId, Pageable pageable);

    @EntityGraph(attributePaths = {"mainCategories.hashtag"})
    List<Profile> findByUser_Role(Role role);

    Optional<Profile> findByUser(User user); // User 객체를 통해 Profile 조회

    Remapper findByUnivNameAndHashtags(List<String> hashtags, int size, PageRequest pageable);
}

