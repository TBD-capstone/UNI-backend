package uni.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uni.backend.domain.User;
import uni.backend.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void validateDuplicateUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }
}
