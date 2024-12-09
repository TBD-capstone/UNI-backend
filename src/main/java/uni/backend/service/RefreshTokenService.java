package uni.backend.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.RefreshToken;
import uni.backend.domain.User;
import uni.backend.repository.RefreshTokenRepository;
import uni.backend.repository.UserRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Getter
    @Value("${jwt.refreshExpirationMs}")
    private Long refreshTokenExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshToken createRefreshToken(Integer userId) {
        if (refreshTokenExpirationMs == null) {
            throw new IllegalStateException("refreshTokenExpirationMs is not initialized");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenExpirationMs));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new IllegalArgumentException("Refresh token has expired");
        }

        if (refreshToken.isRevoked()) {
            throw new IllegalArgumentException("Refresh token is revoked");
        }

        return refreshToken;
    }

    public void deleteByToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        refreshTokenRepository.delete(refreshToken); // DB에서 토큰 삭제
    }

    @Transactional
    @Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시에 실행
    public void removeExpiredTokens() {
        refreshTokenRepository.deleteAllByExpiresAtBefore(Instant.now());
    }
}
