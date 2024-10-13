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
    public User signUp(User user) {
        return userRepository.save(user);
    }
}
