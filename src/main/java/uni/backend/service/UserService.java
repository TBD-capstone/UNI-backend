package uni.backend.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import uni.backend.domain.User;

import java.util.Optional;

public interface UserService extends UserDetailsService {

    User saveUser(User user);

    Optional<User> findByEmail(String email); // 이메일로 유저 조회

    User findById(Integer userId);

    void generateAndSendResetCode(String email);

    boolean verifyResetCode(String email, String token);

    void resetPassword(String email, String newPassword);
}