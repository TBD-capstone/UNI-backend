package uni.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uni.backend.domain.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);
}