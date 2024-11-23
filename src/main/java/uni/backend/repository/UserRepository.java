package uni.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uni.backend.domain.Role;
import uni.backend.domain.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    boolean existsByEmail(String email);

    Optional<User> findById(Integer userId);

}