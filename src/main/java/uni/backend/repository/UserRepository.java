package uni.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uni.backend.domain.Role;
import uni.backend.domain.User;

import java.util.List;
import uni.backend.domain.UserStatus;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByEmail(String email);

    List<User> findByRole(Role role); // Role 기반 조회

    boolean existsByEmail(String email);

    Optional<User> findById(Integer userId);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    List<User> findByStatusAndEndBanDateBefore(UserStatus status, LocalDateTime endBanDate);

}