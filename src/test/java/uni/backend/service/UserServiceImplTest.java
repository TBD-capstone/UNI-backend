package uni.backend.service;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.*;
import uni.backend.domain.dto.MeResponse;
import uni.backend.exception.UserStatusException;
import uni.backend.repository.UserRepository;
import uni.backend.service.UserServiceImpl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;


public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private User mockUser;

    @Mock
    private Profile mockProfile;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "newpassword";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("사용자 저장 테스트")
    void saveUser() {
        User user = new User();
        user.setEmail(TEST_EMAIL);
        user.setPassword(TEST_PASSWORD);

        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.saveUser(user);

        assertNotNull(savedUser);
        assertEquals(TEST_EMAIL, savedUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("이메일로 사용자 로드 성공 테스트")
    void loadUserByUsername_success() {
        User user = new User();
        user.setEmail(TEST_EMAIL);
        user.setPassword(TEST_PASSWORD);
        user.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername(TEST_EMAIL);

        assertNotNull(userDetails);
        assertEquals(TEST_EMAIL, userDetails.getUsername());
    }

    @Test
    @DisplayName("이메일로 사용자 로드 실패 테스트 - 사용자 없음")
    void loadUserByUsername_userNotFound() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
            () -> userService.loadUserByUsername(TEST_EMAIL));
    }

    @Test
    @DisplayName("이메일로 사용자 로드 실패 테스트 - 계정 차단됨")
    void loadUserByUsername_bannedUser() {
        User bannedUser = new User();
        bannedUser.setEmail(TEST_EMAIL);
        bannedUser.setStatus(UserStatus.BANNED);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(bannedUser));

        assertThrows(UserStatusException.class, () -> userService.loadUserByUsername(TEST_EMAIL));
    }

    @Test
    @DisplayName("비밀번호 재설정 성공 테스트")
    void resetPassword_success() {
        User user = new User();
        user.setEmail(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));

        userService.resetPassword(TEST_EMAIL, TEST_PASSWORD);

        verify(userRepository).save(user);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches(TEST_PASSWORD, user.getPassword()));
    }

    @Test
    @DisplayName("현재 사용자 프로필 조회 성공 테스트")
    void getCurrentUserProfile_success_withManualContext() {
        // Given: SecurityContext에 인증 정보 수동 설정
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(TEST_EMAIL, null,
                List.of(new SimpleGrantedAuthority("ROLE_KOREAN")))
        );

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockUser));
        when(mockUser.getEmail()).thenReturn(TEST_EMAIL);
        when(mockUser.getName()).thenReturn("Test User");
        when(mockUser.getRole()).thenReturn(Role.KOREAN);
        when(mockUser.getProfile()).thenReturn(mockProfile);

        // When
        MeResponse response = userService.getCurrentUserProfile();

        // Then
        assertEquals("Test User", response.getName());
        assertEquals(Role.KOREAN, response.getRole());
        verify(userRepository).findByEmail(TEST_EMAIL);
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
