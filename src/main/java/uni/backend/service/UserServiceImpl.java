package uni.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Profile;
import uni.backend.domain.User;
import uni.backend.domain.UserStatus;
import uni.backend.domain.dto.MeResponse;
import uni.backend.exception.UserStatusException;
import uni.backend.repository.UserRepository;
import uni.backend.security.JwtUtils;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public User saveUser(User user) {
        Profile profile = new Profile();
        profile.setUser(user);
        user.setProfile(profile);
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getStatus() == UserStatus.BANNED) {
            log.warn("Banned user {} tried to log in", user.getEmail());
            throw new UserStatusException("이 계정은 제재 되었습니다.");
        }

        return user;
    }

    @Override
    public void generateAndSendResetCode(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("No user found with email: " + email);
        }

        String resetToken = jwtUtils.generateJwtToken(email);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("UNI <jdragon@uni-ajou.site>");
        message.setTo(email);
        message.setSubject("Password Reset Token");
        message.setText("Your password reset token is: " + resetToken);
        mailSender.send(message);
    }

    @Override
    public boolean verifyResetCode(String email, String token) {
        try {
            String tokenEmail = jwtUtils.getEmailFromJwtToken(token);
            return email.equals(tokenEmail) && userRepository.findByEmail(email).isPresent();
        } catch (Exception e) {
            log.error("Invalid reset token or email mismatch: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("No user found with email: " + email));

        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        userRepository.save(user);
    }

    public MeResponse getCurrentUserProfile() {
        // SecurityContext 에서 현재 사용자 가져오기
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다."));

        return MeResponse.builder()
            .userId(currentUser.getUserId())
            .name(currentUser.getName())
            .role(currentUser.getRole())
            .imgProf(
                currentUser.getProfile() != null ? currentUser.getProfile().getImgProf() : null)
            .build();
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
}
