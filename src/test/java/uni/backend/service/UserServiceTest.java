package uni.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uni.backend.domain.Role;
import uni.backend.domain.User;
import uni.backend.repository.UserRepository;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    private Profile profile;

    @Test
    void 회원가입() {
        User user = new User(
                null,
                "test@test.com",
                "123456",
                "test",
                null,
                "ACTIVE",
                Role.KOREAN
        );
        userRepository.save(user);
    }

}