package uni.backend.domain.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uni.backend.domain.User;
import uni.backend.domain.Role;
import uni.backend.domain.UserStatus;


@Component
@RequiredArgsConstructor
public class AdminAccountUtil {

    private static final String ADMIN_PREFIX = "SYSTEM_ADMIN";
    public static final String ADMIN_EMAIL = "admin@uni.site"; // 관리자 고정 이메일
    private static final int SECURITY_DIGIT_NUMBER = 6;
    private static final int RANDOM_NUMBER_BOUND = 10;

    private final PasswordEncoder passwordEncoder;

    /**
     * 관리자 계정 생성
     *
     * @return 생성된 User 엔티티 (관리자)
     */
    public User createAdminAccount(String rawPassword) {
        User admin = new User();
        admin.setEmail(ADMIN_EMAIL); // 고정된 관리자 이메일
        admin.setPassword(passwordEncoder.encode(rawPassword)); // 암호화된 비밀번호 저장
        admin.setStatus(UserStatus.ACTIVE);
        admin.setRole(Role.ADMIN);
        admin.setName("System Admin");
        admin.setAdminId(UUID.randomUUID().toString()); // UUID로 고유 관리 ID 생성
        return admin;
    }

    /**
     * 랜덤한 관리자 비밀번호 생성
     */
    public String createAdminPassword() {
        return UUID.randomUUID().toString() + createRandomCodeNumber();
    }

    /**
     * 랜덤 숫자 생성 로직 (보안 강화를 위해 SecureRandom 사용)
     */
    private String createRandomCodeNumber() {
        try {
            Random random = SecureRandom.getInstanceStrong();
            return createRandomNumber(random);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SecureRandom instance could not be created.", e);
        }
    }

    /**
     * 랜덤 숫자 시퀀스 생성
     */
    private String createRandomNumber(Random random) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < SECURITY_DIGIT_NUMBER; i++) {
            builder.append(random.nextInt(RANDOM_NUMBER_BOUND));
        }
        return builder.toString();
    }
}
