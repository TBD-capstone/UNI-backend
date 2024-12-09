package uni.backend.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import uni.backend.domain.Profile;
import uni.backend.domain.Role;
import uni.backend.domain.User;
import uni.backend.domain.UserStatus;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAccountUtilTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminAccountUtil adminAccountUtil;

    @Test
    void createAdminAccount_ShouldCreateValidAdmin() {
        String rawPassword = "rawPassword";
        String encodedPassword = "encodedPassword";
        Mockito.when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        User admin = adminAccountUtil.createAdminAccount(rawPassword);

        assertNotNull(admin);
        assertEquals("System Admin", admin.getName());
        assertEquals(UserStatus.ACTIVE, admin.getStatus());
        assertEquals(Role.ADMIN, admin.getRole());
        assertNotNull(admin.getAdminId());
        assertEquals(encodedPassword, admin.getPassword());
        assertNotNull(admin.getProfile());
        assertEquals("/profile-image.png", admin.getProfile().getImgProf());
    }

    @Test
    void createAdminName_ShouldReturnValidEmail() {
        String email = adminAccountUtil.createAdminName();
        assertTrue(email.matches("admin\\d{6}@uni-ajou.site"));
    }

    @Test
    void createAdminPassword_ShouldReturnValidPassword() {
        String password = adminAccountUtil.createAdminPassword();
        assertNotNull(password);
        assertTrue(password.length() > 10); // UUID와 랜덤 코드 결합
    }

    @Test
    void createRandomCodeNumber_ShouldGenerateValidCode() {
        String randomCode = adminAccountUtil.createRandomCodeNumber();
        assertNotNull(randomCode);
        assertEquals(6, randomCode.length());
        assertTrue(randomCode.matches("\\d{6}")); // 숫자로만 구성
    }

    @Test
    void createRandomCodeNumber_ShouldThrowException_WhenNoAlgorithm() {
        try (MockedStatic<SecureRandom> mockedRandom = mockStatic(SecureRandom.class)) {
            mockedRandom.when(SecureRandom::getInstanceStrong)
                .thenThrow(NoSuchAlgorithmException.class);
            assertThrows(RuntimeException.class, () -> adminAccountUtil.createRandomCodeNumber());
        }
    }

    @Test
    void createEmailForm_ShouldCreateValidEmailMessage() {
        String toEmail = "admin@uni-ajou.site";
        String adminName = "admin123";
        String password = "securePassword";

        SimpleMailMessage message = AdminAccountUtil.createEmailForm(toEmail, adminName, password);

        assertNotNull(message);
        assertEquals(toEmail, message.getTo()[0]);
        assertEquals("[UNI] PLEASE Don't expose your password", message.getSubject());
        assertEquals("[Admin Account: admin123], [Password: securePassword]", message.getText());
    }

    @Test
    void generateRandomNumberSequence_ShouldReturnValidSequence() {
        SecureRandom secureRandom = mock(SecureRandom.class);
        when(secureRandom.nextInt(10)).thenReturn(1, 2, 3, 4, 5, 6);

        String sequence = adminAccountUtil.generateRandomNumberSequence(secureRandom);

        assertEquals("123456", sequence);
    }
}
