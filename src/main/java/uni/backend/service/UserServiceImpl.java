package uni.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import uni.backend.domain.Role;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Profile;
import uni.backend.domain.User;
import uni.backend.repository.UserRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final Map<String, String> resetCodes = new HashMap<>();

    @Override
    @Transactional
    public User saveUser(User user) {
        // validateDuplicateUser(user); // 추후 이메일 api 인증시에 이 부분 활성화 하면 될 것 같습니다.
        Profile profile = new Profile();
        profile.setUser(user);
        user.setProfile(profile);
        return userRepository.save(user);
    }

    @Override
    public void validateDuplicateUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findKoreanUsers() {
        return userRepository.findByRole(Role.KOREAN);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findById(Integer userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자를 찾을 수 없습니다."));
    }

    @Override
    public void generateAndSendResetCode(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("No user found with email: " + email);
        }

        String code = String.format("%06d", new Random().nextInt(999999));
        resetCodes.put(email, code);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("UNI <jdragon@uni-ajou.site>");
        message.setTo(email);
        message.setSubject("Password Reset Code");
        message.setText("Your password reset code is: " + code);
        mailSender.send(message);
    }

    @Override
    public boolean verifyResetCode(String email, String code) {
        return resetCodes.containsKey(email) && resetCodes.get(email).equals(code);
    }

    @Override
    public void resetPassword(String email, String newPassword) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("No user found with email: " + email);
        }

        User user = userOptional.get();
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        userRepository.save(user);
        resetCodes.remove(email);
    }
}
