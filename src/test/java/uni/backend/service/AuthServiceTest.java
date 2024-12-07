package uni.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import uni.backend.domain.Profile;
import uni.backend.domain.RefreshToken;
import uni.backend.domain.Role;
import uni.backend.domain.User;
import uni.backend.domain.dto.LoginRequest;
import uni.backend.domain.dto.LoginResponse;
import uni.backend.domain.dto.MeResponse;
import uni.backend.security.JwtUtils;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void givenUserWithKoreanRole_whenLogin_thenIsKoreanTrue() {
        // Given
        String email = "user@uni.com";
        String password = "password";

        LoginRequest loginRequest = LoginRequest.builder()
            .email(email)
            .password(password)
            .build();

        User user = new User();
        user.setEmail(email);
        user.setUserId(1);
        user.setName("John Doe");
        user.setRole(Role.KOREAN); // Role 설정
        Profile profile = new Profile();
        profile.setImgProf("img_prof.jpg");
        profile.setImgBack("img_back.jpg");
        user.setProfile(profile);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(email, Role.KOREAN)).thenReturn("access-token");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(3600));

        when(refreshTokenService.createRefreshToken(1)).thenReturn(refreshToken);

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.getIsKorean()); // isKorean 확인
    }

    @Test
    void givenUserWithNonKoreanRole_whenLogin_thenIsKoreanFalse() {
        // Given
        String email = "user@uni.com";
        String password = "password";

        LoginRequest loginRequest = LoginRequest.builder()
            .email(email)
            .password(password)
            .build();

        User user = new User();
        user.setEmail(email);
        user.setUserId(1);
        user.setName("John Doe");
        user.setRole(Role.ADMIN); // Role 설정
        Profile profile = new Profile();
        profile.setImgProf("img_prof.jpg");
        profile.setImgBack("img_back.jpg");
        user.setProfile(profile);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(email, Role.ADMIN)).thenReturn("access-token");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(3600));

        when(refreshTokenService.createRefreshToken(1)).thenReturn(refreshToken);

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertFalse(response.getIsKorean()); // isKorean 확인
    }

    @Test
    void givenValidRefreshToken_whenRefreshAccessToken_thenReturnNewAccessToken() {
        // Given
        String refreshTokenValue = "valid-refresh-token";
        User user = new User();
        user.setEmail("user@uni.com");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(3600));

        when(refreshTokenService.verifyRefreshToken(refreshTokenValue)).thenReturn(refreshToken);
        when(jwtUtils.generateJwtToken("user@uni.com")).thenReturn("new-access-token");

        // When
        Map<String, String> tokens = authService.refreshAccessToken(refreshTokenValue);

        // Then
        assertNotNull(tokens);
        assertEquals("new-access-token", tokens.get("accessToken"));
        assertEquals(refreshTokenValue, tokens.get("refreshToken"));
        verify(refreshTokenService, times(1)).verifyRefreshToken(refreshTokenValue);
        verify(jwtUtils, times(1)).generateJwtToken("user@uni.com");
    }

    @Test
    void givenToken_whenLogout_thenTokenDeleted() {
        // Given
        String token = "some-refresh-token";

        // When
        authService.logout(token);

        // Then
        verify(refreshTokenService, times(1)).deleteByToken(token);
    }

    @Test
    void givenNullToken_whenLogout_thenDoNothing() {
        // When
        authService.logout(null);

        // Then
        verify(refreshTokenService, never()).deleteByToken(anyString());
    }

    @Test
    void givenLoggedInUser_whenGetLoggedInUserInfo_thenReturnMeResponse() {
        // Given
        User user = new User();
        user.setUserId(1);
        user.setName("John Doe");
        user.setRole(Role.KOREAN);
        Profile profile = new Profile();
        profile.setImgProf("img_prof.jpg");
        user.setProfile(profile);

        // When
        MeResponse response = authService.getLoggedInUserInfo(user);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getUserId());
        assertEquals("John Doe", response.getName());
        assertEquals(Role.KOREAN, response.getRole());
        assertEquals("img_prof.jpg", response.getImgProf());
    }

    @Test
    void givenNullUser_whenGetLoggedInUserInfo_thenThrowException() {
        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> authService.getLoggedInUserInfo(null)
        );
        assertEquals("User is not logged in.", exception.getMessage());
    }
}
