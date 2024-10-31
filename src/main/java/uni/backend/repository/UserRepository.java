package uni.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uni.backend.domain.Role;
import uni.backend.domain.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);
    List<User> findByRole(Role role); // Role 기반 조회
}