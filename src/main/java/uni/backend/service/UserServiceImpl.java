package uni.backend.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
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

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final Map<String, String> resetCodes = new HashMap<>();

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

    public MeResponse getCurrentUserProfile() {
        // SecurityContext에서 현재 사용자 가져오기
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

    public User getUserBySessionId(String sessionId, HttpSession session) {
        // 세션 아이디 확인
        if (!sessionId.equals(session.getId())) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        // SecurityContext를 가져옴
        SecurityContext securityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
        if (securityContext == null) {
            throw new IllegalStateException("No security context found for session ID: " + sessionId);
        }

        // Authentication 객체를 가져옴
        Authentication authentication = securityContext.getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found for session ID: " + sessionId);
        }

        // 인증된 사용자 정보 반환
        return (User) authentication.getPrincipal();
    }
}
