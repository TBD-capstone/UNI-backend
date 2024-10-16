package uni.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Profile;
import uni.backend.domain.User;
import uni.backend.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User signUp(User user) {
        Profile profile = new Profile();
        profile.setUser(user);
        user.setProfile(profile);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User findUser(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
