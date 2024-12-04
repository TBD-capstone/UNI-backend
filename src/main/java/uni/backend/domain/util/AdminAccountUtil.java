package uni.backend.domain.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uni.backend.config.SecurityConfig;
import uni.backend.domain.User;
import uni.backend.domain.Role;
import uni.backend.domain.UserStatus;

@Component
public class AdminAccountUtil {

    private static final String TITLE_MESSAGE = "[UNI] PLEASE Don't expose your password";
    private static final int SECURITY_DIGIT_NUMBER = 6; // 랜덤 숫자 자리수
    private static final int RANDOM_NUMBER_BOUND = 10; // 랜덤 숫자의 최대값
    private static final String ADMIN_PREFIX = "ADMIN"; // 관리자 계정 접두사

    private final PasswordEncoder passwordEncoder;
    private final SecurityConfig securityConfig;

    @Autowired
    public AdminAccountUtil(PasswordEncoder passwordEncoder,
        @Lazy SecurityConfig securityConfig) {
        this.passwordEncoder = passwordEncoder;
        this.securityConfig = securityConfig;
    }

    /**
     * 관리자 계정 생성
     *
     * @param rawPassword 생성된 비밀번호
     * @return 생성된 User 객체
     */
    public User createAdminAccount(String rawPassword) {
        User admin = new User();
        admin.setEmail(createAdminName()); // 고유한 관리자 이메일 생성
        admin.setPassword(passwordEncoder.encode(rawPassword)); // 비밀번호 암호화 후 저장
        admin.setStatus(UserStatus.ACTIVE); // 활성화 상태 설정
        admin.setRole(Role.ADMIN); // 역할을 관리자(Admin)로 설정
        admin.setName("System Admin"); // 관리자 기본 이름 설정
        admin.setAdminId(UUID.randomUUID().toString()); // 고유 Admin ID 설정
        return admin;
    }

    /**
     * 관리자 이메일 생성
     *
     * @return 고유한 이메일 문자열
     */
    public String createAdminName() {
        String randomSuffix = String.format("%06d", new Random().nextInt(999999));
        return String.format("admin%s@uni-ajou.site", randomSuffix);
    }

    /**
     * 랜덤한 관리자 비밀번호 생성
     *
     * @return 생성된 비밀번호
     */
    public String createAdminPassword() {
        return UUID.randomUUID().toString() + createRandomCodeNumber();
    }

    /**
     * 랜덤 숫자 생성
     *
     * @return 보안 강화된 랜덤 숫자 문자열
     */
    private String createRandomCodeNumber() {
        try {
            Random random = SecureRandom.getInstanceStrong();
            return generateRandomNumberSequence(random);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SecureRandom instance could not be created.", e);
        }
    }

    /**
     * 랜덤 숫자 시퀀스 생성
     *
     * @param random 랜덤 생성기
     * @return 랜덤 숫자 문자열
     */
    private String generateRandomNumberSequence(Random random) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < SECURITY_DIGIT_NUMBER; i++) {
            builder.append(random.nextInt(RANDOM_NUMBER_BOUND));
        }
        return builder.toString();
    }

    /**
     * 이메일 메시지 생성
     *
     * @param toEmail   수신자 이메일
     * @param adminName 관리자 이름
     * @param password  관리자 비밀번호
     * @return 이메일 메시지 객체
     */
    public static SimpleMailMessage createEmailForm(String toEmail, String adminName,
        String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(TITLE_MESSAGE);
        message.setText(formatEmailText(adminName, password));
        return message;
    }

    /**
     * 이메일 본문 텍스트 포맷
     *
     * @param adminName 관리자 이름
     * @param password  관리자 비밀번호
     * @return 포맷된 이메일 텍스트
     */
    private static String formatEmailText(String adminName, String password) {
        return String.format("[Admin Account: %s], [Password: %s]", adminName, password);
    }
}
