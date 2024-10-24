package uni.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uni.backend.domain.Role;
import uni.backend.domain.User;
import uni.backend.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User saveUser(User user) {
        validateDuplicateUser(user);
        return userRepository.save(user);
    }

    @Override
    public void validateDuplicateUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll(); // 모든 회원 조회
    }

    @Override
    public List<User> findKoreanUsers() {
        return userRepository.findByRole(Role.KOREAN);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
