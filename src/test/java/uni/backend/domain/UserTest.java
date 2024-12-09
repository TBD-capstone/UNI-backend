package uni.backend.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import uni.backend.domain.dto.SignupRequest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setName("Test User");
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(Role.KOREAN);
    }

    @Test
    void isEnabled_ShouldReturnTrue_WhenUserIsActiveAndNotBanned() {
        // Arrange
        user.setStatus(UserStatus.ACTIVE);
        user.setEndBanDate(null);

        // Act & Assert
        assertTrue(user.isEnabled());
    }

    @Test
    void isEnabled_ShouldReturnFalse_WhenUserIsBanned() {
        // Arrange
        user.setStatus(UserStatus.ACTIVE);
        user.setEndBanDate(LocalDateTime.now().plusDays(1)); // 현재 시간 이후로 설정

        // Act & Assert
        assertFalse(user.isEnabled());
    }

    @Test
    void banUser_ShouldSetBannedStatus() {
        // Arrange
        LocalDateTime banUntil = LocalDateTime.now().plusDays(7);
        String reason = "Violation of terms";

        // Act
        user.banUser(banUntil, reason);

        // Assert
        assertEquals(UserStatus.BANNED, user.getStatus());
        assertEquals(banUntil, user.getEndBanDate());
        assertEquals(reason, user.getLastReportReason());
    }

    @Test
    void unbanUser_ShouldResetBanStatus() {
        // Arrange
        user.banUser(LocalDateTime.now().plusDays(7), "Violation");

        // Act
        user.unbanUser();

        // Assert
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNull(user.getEndBanDate());
        assertNull(user.getLastReportReason());
    }

    @Test
    void incrementReportCount_ShouldIncreaseReportCountAndSetReason() {
        // Arrange
        String reason = "Inappropriate behavior";

        // Act
        user.incrementReportCount(reason);

        // Assert
        assertEquals(1L, user.getReportCount());
        assertEquals(reason, user.getLastReportReason());
    }

    @Test
    void incrementReportCount_ShouldAutoBanUser_WhenReportCountExceedsThreshold() {
        // Arrange
        String reason = "Repeated violations";

        // Act
        user.incrementReportCount(reason);
        user.incrementReportCount(reason);
        user.incrementReportCount(reason); // 3번째 신고

        // Assert
        assertEquals(3L, user.getReportCount());
        assertEquals(UserStatus.BANNED, user.getStatus());
        assertNotNull(user.getEndBanDate());
        assertEquals(reason, user.getLastReportReason());
    }

    @Test
    void createUser_ShouldCreateUserWithCorrectFields() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("newpassword");
        signupRequest.setName("New User");
        signupRequest.setUnivName("Example University");
        signupRequest.setIsKorean(true);

        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedpassword");

        // Act
        User createdUser = User.createUser(signupRequest, passwordEncoder);

        // Assert
        assertEquals("newuser@example.com", createdUser.getEmail());
        assertEquals("encodedpassword", createdUser.getPassword());
        assertEquals("New User", createdUser.getName());
        assertEquals("Example University", createdUser.getUnivName());
        assertEquals(UserStatus.ACTIVE, createdUser.getStatus());
        assertEquals(Role.KOREAN, createdUser.getRole());
    }
}
