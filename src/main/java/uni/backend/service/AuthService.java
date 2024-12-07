package uni.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import uni.backend.domain.RefreshToken;
import uni.backend.domain.Role;
import uni.backend.domain.User;
import uni.backend.domain.dto.LoginRequest;
import uni.backend.domain.dto.LoginResponse;
import uni.backend.domain.dto.MeResponse;
import uni.backend.security.JwtUtils;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
                loginRequest.getPassword())
        );
        User user = (User) authentication.getPrincipal();
        String accessToken = jwtUtils.generateJwtToken(user.getEmail(), user.getRole());
        String refreshToken = refreshTokenService.createRefreshToken(user.getUserId()).getToken();
        return new LoginResponse(
            "success", "logged in successfully", user.getName(),
            user.getUserId(), user.getRole() == Role.KOREAN,
            user.getProfile().getImgProf(), user.getProfile().getImgBack(),
            accessToken, refreshToken
        );
    }

    public Map<String, String> refreshAccessToken(String refreshToken) {
        // RefreshToken 검증
        RefreshToken token = refreshTokenService.verifyRefreshToken(refreshToken);

        // 새 AccessToken 생성
        String newAccessToken = jwtUtils.generateJwtToken(token.getUser().getEmail());

        // 응답 데이터 반환
        return Map.of(
            "accessToken", newAccessToken,
            "refreshToken", token.getToken()
        );
    }

    public void logout(String token) {
        if (token != null) {
            refreshTokenService.deleteByToken(token);
        }
    }

    public MeResponse getLoggedInUserInfo(User user) {
        if (user == null) {
            throw new IllegalStateException("User is not logged in.");
        }

        return MeResponse.builder()
            .userId(user.getUserId())
            .name(user.getName())
            .role(user.getRole())
            .imgProf(user.getProfile().getImgProf())
            .build();
    }
}

