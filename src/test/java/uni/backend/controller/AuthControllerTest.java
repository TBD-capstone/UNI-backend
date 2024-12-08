package uni.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uni.backend.domain.Role;
import uni.backend.domain.User;
import uni.backend.domain.dto.*;
import uni.backend.security.JwtUtils;
import uni.backend.service.AuthService;
import uni.backend.service.UserService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void givenValidSignupRequest_whenSignup_thenReturnSuccessResponse() {
        // Given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("John Doe");
        signupRequest.setEmail("user@uni.com");
        signupRequest.setPassword("password");
        signupRequest.setIsKorean(true);
        signupRequest.setUnivName("Uni Name");

        User user = new User(); // 실제 User 객체 생성
        when(userService.saveUser(any(User.class))).thenReturn(user); // User 저장을 Mock

        // When
        ResponseEntity<Response> response = authController.signup(signupRequest);

        // Then
        assertNotNull(response);
        assertEquals("signed up successfully", response.getBody().getMessage());
        assertEquals("success", response.getBody().getStatus());
        verify(userService, times(1)).saveUser(any(User.class));
    }

    @Test
    void givenDuplicateUser_whenSignup_thenReturnFailureResponse() {
        // Given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("John Doe");
        signupRequest.setEmail("user@uni.com");
        signupRequest.setPassword("password");
        signupRequest.setIsKorean(true);
        signupRequest.setUnivName("Uni Name");

        doThrow(new IllegalStateException("User already exists"))
            .when(userService).saveUser(any(User.class));

        // When
        ResponseEntity<Response> response = authController.signup(signupRequest);

        // Then
        assertNotNull(response);
        assertEquals("signed up failed", response.getBody().getMessage());
        assertEquals("fail", response.getBody().getStatus());
        verify(userService, times(1)).saveUser(any(User.class));
    }

    @Test
    void givenValidLoginRequest_whenLogin_thenReturnLoginResponse() {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
            .email("user@uni.com")
            .password("password")
            .build();

        LoginResponse loginResponse = new LoginResponse("success", "logged in successfully",
            "John Doe", 1, Role.KOREAN, "img_prof.jpg", "img_back.jpg", "access-token",
            "refresh-token");

        when(authService.login(loginRequest)).thenReturn(loginResponse);

        // When
        ResponseEntity<LoginResponse> response = authController.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals(loginResponse, response.getBody());
        verify(authService, times(1)).login(loginRequest);
    }

    @Test
    void givenValidRefreshTokenRequest_whenRefreshToken_thenReturnNewTokens() {
        // Given
        Map<String, String> request = Map.of("refreshToken", "valid-refresh-token");
        Map<String, String> tokens = Map.of("accessToken", "new-access-token", "refreshToken",
            "valid-refresh-token");

        when(authService.refreshAccessToken("valid-refresh-token")).thenReturn(tokens);

        // When
        ResponseEntity<?> response = authController.refreshToken(request);

        // Then
        assertNotNull(response);
        assertEquals(tokens, response.getBody());
        verify(authService, times(1)).refreshAccessToken("valid-refresh-token");
    }

    @Test
    void givenValidRequest_whenLogout_thenReturnSuccessResponse() {
        // Given
        String token = "valid-token";
        when(jwtUtils.getJwtFromRequest(any())).thenReturn(token);
        doNothing().when(authService).logout(token);

        // When
        ResponseEntity<?> response = authController.logout(mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertEquals("success", ((Response) response.getBody()).getStatus());
        assertEquals("Logged out successfully", ((Response) response.getBody()).getMessage());
        verify(authService, times(1)).logout(token);
    }

    @Test
    void givenValidUser_whenLoginCheck_thenReturnMeResponse() {
        // Given
        User user = mock(User.class);
        MeResponse meResponse = MeResponse.builder()
            .userId(1)
            .name("John Doe")
            .role(Role.KOREAN)
            .imgProf("img_prof.jpg")
            .build();

        when(authService.getLoggedInUserInfo(user)).thenReturn(meResponse);

        // When
        ResponseEntity<?> response = authController.loginCheck(user);

        // Then
        assertNotNull(response);
        assertEquals(meResponse, response.getBody());
        verify(authService, times(1)).getLoggedInUserInfo(user);
    }

    @Test
    void givenNullUser_whenLoginCheck_thenReturnUnauthorized() {
        // Given
        when(authService.getLoggedInUserInfo(null)).thenThrow(
            new IllegalStateException("User is not logged in"));

        // When
        ResponseEntity<?> response = authController.loginCheck(null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("User is not logged in", response.getBody());
    }

    @Test
    void givenValidForgotPasswordRequest_whenSendResetCode_thenReturnSuccessResponse() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("user@uni.com");
        doNothing().when(userService).generateAndSendResetCode(request.getEmail());

        // When
        ResponseEntity<Response> response = authController.sendResetCode(request);

        // Then
        assertNotNull(response);
        assertEquals("Reset code sent to email", response.getBody().getMessage());
        assertEquals("success", response.getBody().getStatus());
        verify(userService, times(1)).generateAndSendResetCode(request.getEmail());
    }

    @Test
    void givenInvalidForgotPasswordRequest_whenSendResetCode_thenReturnFailureResponse() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("user@uni.com");
        doThrow(new RuntimeException("Invalid email")).when(userService)
            .generateAndSendResetCode(request.getEmail());

        // When
        ResponseEntity<Response> response = authController.sendResetCode(request);

        // Then
        assertNotNull(response);
        assertEquals("Invalid email", response.getBody().getMessage());
        assertEquals("fail", response.getBody().getStatus());
        verify(userService, times(1)).generateAndSendResetCode(request.getEmail());
    }

    @Test
    void givenValidResetPasswordRequest_whenResetPassword_thenReturnSuccessResponse() {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("user@uni.com", "valid-code",
            "new-password");

        when(userService.verifyResetCode(request.getEmail(), request.getCode())).thenReturn(true);
        doNothing().when(userService).resetPassword(request.getEmail(), request.getNewPassword());

        // When
        ResponseEntity<Response> response = authController.resetPassword(request);

        // Then
        assertNotNull(response);
        assertEquals("Password has been reset", response.getBody().getMessage());
        assertEquals("success", response.getBody().getStatus());
        verify(userService, times(1)).verifyResetCode(request.getEmail(), request.getCode());
        verify(userService, times(1)).resetPassword(request.getEmail(), request.getNewPassword());
    }

    @Test
    void givenInvalidResetCode_whenResetPassword_thenReturnFailureResponse() {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("user@uni.com", "invalid-code",
            "new-password");

        when(userService.verifyResetCode(request.getEmail(), request.getCode())).thenReturn(false);

        // When
        ResponseEntity<Response> response = authController.resetPassword(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid reset code", response.getBody().getMessage());
        assertEquals("fail", response.getBody().getStatus());
        verify(userService, times(1)).verifyResetCode(request.getEmail(), request.getCode());
        verify(userService, never()).resetPassword(anyString(), anyString());
    }

    @Test
    void givenValidResetCode_whenResetPasswordThrowsException_thenReturnInternalServerError() {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("user@uni.com", "valid-code",
            "new-password");

        when(userService.verifyResetCode(request.getEmail(), request.getCode())).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(userService)
            .resetPassword(request.getEmail(), request.getNewPassword());

        // When
        ResponseEntity<Response> response = authController.resetPassword(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody().getMessage());
        assertEquals("fail", response.getBody().getStatus());
        verify(userService, times(1)).verifyResetCode(request.getEmail(), request.getCode());
        verify(userService, times(1)).resetPassword(request.getEmail(), request.getNewPassword());
    }
}