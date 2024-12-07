package uni.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import uni.backend.domain.Profile;
import uni.backend.domain.Role;
import uni.backend.domain.User;
import uni.backend.domain.UserStatus;
import uni.backend.domain.dto.MeResponse;
import uni.backend.exception.UserStatusException;
import uni.backend.repository.UserRepository;
import uni.backend.security.JwtUtils;

import java.util.Optional;

import static com.amazonaws.services.ec2.model.PrincipalType.Role;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
  
    @Test
    void givenUser_whenSaveUser_thenUserSavedWithProfile() {
        // Given
        User user = new User();
        user.setEmail("test@uni.com");
        when(userRepository.save(user)).thenReturn(user);

        // When
        User savedUser = userService.saveUser(user);

        // Then
        assertNotNull(savedUser.getProfile());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenExistingEmail_whenLoadUserByUsername_thenUserReturned() {
        // Given
        String email = "test@uni.com";
        User user = new User();
        user.setEmail(email);
        user.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        var userDetails = userService.loadUserByUsername(email);

        // Then
        assertEquals(email, userDetails.getUsername());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void givenBannedUser_whenLoadUserByUsername_thenThrowException() {
        // Given
        String email = "banned@uni.com";
        User user = new User();
        user.setEmail(email);
        user.setStatus(UserStatus.BANNED);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(UserStatusException.class, () -> userService.loadUserByUsername(email));
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void givenEmail_whenGenerateAndSendResetCode_thenEmailSent() {
        // Given
        String email = "reset@uni.com";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtils.generateJwtToken(email)).thenReturn("test-token");

        // Mock mailSender behavior
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        userService.generateAndSendResetCode(email);

        // Then
        verify(userRepository, times(1)).findByEmail(email);
        verify(jwtUtils, times(1)).generateJwtToken(email);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void givenNonExistingEmail_whenGenerateAndSendResetCode_thenThrowException() {
        // Given
        String email = "nonexistent@uni.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.generateAndSendResetCode(email)
        );

        assertEquals("No user found with email: " + email, exception.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
        verify(jwtUtils, never()).generateJwtToken(anyString());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void givenValidToken_whenVerifyResetCode_thenReturnTrue() {
        // Given
        String email = "valid@uni.com";
        String token = "test-token";
        when(jwtUtils.getEmailFromJwtToken(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        // When
        boolean result = userService.verifyResetCode(email, token);

        // Then
        assertTrue(result);
        verify(jwtUtils, times(1)).getEmailFromJwtToken(token);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void givenInvalidToken_whenVerifyResetCode_thenReturnFalse() {
        // Given
        String email = "invalid@uni.com";
        String token = "test-token";
        when(jwtUtils.getEmailFromJwtToken(token)).thenThrow(new RuntimeException("Invalid token"));

        // When
        boolean result = userService.verifyResetCode(email, token);

        // Then
        assertFalse(result);
        verify(jwtUtils, times(1)).getEmailFromJwtToken(token);
        verify(userRepository, never()).findByEmail(email);
    }

    @Test
    void givenValidToken_whenResetPassword_thenPasswordUpdated() {
        // Given
        String token = "valid-token";
        String email = "reset@uni.com";
        String newPassword = "newPassword";
        User user = new User();
        user.setEmail(email);
        when(jwtUtils.getEmailFromJwtToken(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        userService.resetPassword(token, newPassword);

        // Then
        assertTrue(new BCryptPasswordEncoder().matches(newPassword, user.getPassword()));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenInvalidToken_whenResetPassword_thenThrowException() {
        // Given
        String token = "invalid-token";
        when(jwtUtils.getEmailFromJwtToken(token)).thenThrow(new RuntimeException("Token parsing failed"));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.resetPassword(token, "newPassword")
        );

        assertEquals("Invalid reset token.", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("Token parsing failed", exception.getCause().getMessage());
        verify(jwtUtils, times(1)).getEmailFromJwtToken(token);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void givenAuthenticatedUser_whenGetCurrentUserProfile_thenReturnMeResponse() {
        // Given
        String currentUserEmail = "user@uni.com";
        User user = new User();
        user.setUserId(1);
        user.setEmail(currentUserEmail);
        user.setName("John Doe");
        user.setRole(uni.backend.domain.Role.KOREAN);
        Profile profile = new Profile();
        profile.setImgProf("profile.jpg");
        user.setProfile(profile);

        // Mock SecurityContextHolder
        var authentication = mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn(currentUserEmail);
        var securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock userRepository
        when(userRepository.findByEmail(currentUserEmail)).thenReturn(Optional.of(user));

        // When
        MeResponse response = userService.getCurrentUserProfile();

        // Then
        assertNotNull(response);
        assertEquals(1, response.getUserId());
        assertEquals("John Doe", response.getName());
        assertEquals(uni.backend.domain.Role.KOREAN, response.getRole());
        assertEquals("profile.jpg", response.getImgProf());

        verify(userRepository, times(1)).findByEmail(currentUserEmail);
    }

    @Test
    void givenExistingEmail_whenFindByEmail_thenReturnUser() {
        // Given
        String email = "existing@uni.com";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findByEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void givenNonExistingEmail_whenFindByEmail_thenReturnEmpty() {
        // Given
        String email = "nonexistent@uni.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail(email);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void givenExistingUserId_whenFindById_thenReturnUser() {
        // Given
        Integer userId = 1;
        User user = new User();
        user.setUserId(userId);
        user.setName("John Doe");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        User result = userService.findById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("John Doe", result.getName());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void givenNonExistingUserId_whenFindById_thenThrowException() {
        // Given
        Integer userId = 99;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.findById(userId)
        );

        assertEquals("해당 ID의 사용자를 찾을 수 없습니다.", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("비밀번호 재설정 코드 생성 및 발송 테스트")
    void generateAndSendResetCode_success() {
        // Given
        User user = new User();
        user.setEmail(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        userService.generateAndSendResetCode(TEST_EMAIL);

        // Then
        String generatedCode = userService.resetCodes.get(TEST_EMAIL);
        assertNotNull(generatedCode, "Reset code should be generated");
        assertTrue(userService.verifyResetCode(TEST_EMAIL, generatedCode));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }


    @Test
    @DisplayName("비밀번호 재설정 코드 생성 실패 테스트 - 사용자 없음")
    void generateAndSendResetCode_userNotFound() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class,
            () -> userService.generateAndSendResetCode(TEST_EMAIL));
    }


}
