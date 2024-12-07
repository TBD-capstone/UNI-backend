package uni.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import uni.backend.domain.RefreshToken;
import uni.backend.domain.User;
import uni.backend.repository.RefreshTokenRepository;
import uni.backend.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("private")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private final Long expirationMs = 604800000L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, userRepository);

        // refreshTokenExpirationMs 값 주입
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationMs", expirationMs);
    }

    @Test
    void createRefreshToken_ShouldThrowException_WhenExpirationMsNotInitialized() {
        // given
        Integer userId = 1;

        // ReflectionTestUtils로 expirationMs를 null로 설정
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationMs", null);

        // when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> refreshTokenService.createRefreshToken(userId),
                "refreshTokenExpirationMs가 초기화되지 않았을 때 예외가 발생해야 합니다."
        );

        assertEquals("refreshTokenExpirationMs is not initialized", exception.getMessage());
    }

    @Test
    void createRefreshToken_ShouldReturnRefreshToken() {
        // given
        Integer userId = 1;
        User user = new User();
        user.setUserId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // 저장된 객체 그대로 반환

        // when
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId);

        // then
        assertNotNull(refreshToken, "생성된 RefreshToken이 null이어선 안됩니다.");
        assertEquals(userId, refreshToken.getUser().getUserId(), "RefreshToken에 저장된 User ID가 올바르지 않습니다.");
    }

    @Test
    void createRefreshToken_ShouldThrowException_WhenUserNotFound() {
        // given
        Integer userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> refreshTokenService.createRefreshToken(userId));
    }

    @Test
    void verifyRefreshToken_ShouldReturnToken_WhenValid() {
        // given
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(3600));
        refreshToken.setRevoked(false);

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

        // when
        RefreshToken result = refreshTokenService.verifyRefreshToken(token);

        // then
        assertNotNull(result);
        assertEquals(token, result.getToken());
        verify(refreshTokenRepository, never()).save(refreshToken);
    }

    @Test
    void verifyRefreshToken_ShouldThrowException_WhenTokenExpired() {
        // given
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(Instant.now().minusSeconds(3600));
        refreshToken.setRevoked(false);

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> refreshTokenService.verifyRefreshToken(token));
        assertTrue(refreshToken.isRevoked());
        verify(refreshTokenRepository, times(1)).save(refreshToken);
    }

    @Test
    void verifyRefreshToken_ShouldThrowException_WhenTokenRevoked() {
        // given
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(3600));
        refreshToken.setRevoked(true);

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> refreshTokenService.verifyRefreshToken(token));
    }

    @Test
    void deleteByToken_ShouldDeleteToken() {
        // given
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

        // when
        refreshTokenService.deleteByToken(token);

        // then
        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }

    @Test
    void deleteByToken_ShouldThrowException_WhenTokenNotFound() {
        // given
        String token = UUID.randomUUID().toString();
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> refreshTokenService.deleteByToken(token));
    }

    @Test
    void removeExpiredTokens_ShouldDeleteExpiredTokens() {
        // when
        refreshTokenService.removeExpiredTokens();

        // then
        verify(refreshTokenRepository, times(1)).deleteAllByExpiresAtBefore(any(Instant.class));
    }
}