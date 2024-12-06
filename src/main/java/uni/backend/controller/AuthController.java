package uni.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.User;
import uni.backend.domain.dto.*;
import uni.backend.security.JwtUtils;
import uni.backend.service.AuthService;
import uni.backend.service.UserService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtils jwtUtils;
    private final AuthService authService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/signup")
    public ResponseEntity<Response> signup(@RequestBody SignupRequest signupRequest) {
        User user = User.createUser(signupRequest, passwordEncoder);
        try {
            userService.saveUser(user);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(Response.failMessage("signed up failed"));
        }
        return ResponseEntity.ok(Response.successMessage("signed up successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        Map<String, String> tokens = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = jwtUtils.getJwtFromRequest(request); // 헤더에서 JWT 추출
        authService.logout(token); // 리프레시 토큰 삭제
        return ResponseEntity.ok(new Response("success", "Logged out successfully"));
    }

    @GetMapping("/loginCheck")
    public ResponseEntity<?> loginCheck(@AuthenticationPrincipal User user) {
        try {
            MeResponse meResponse = authService.getLoggedInUserInfo(user);
            return ResponseEntity.ok(meResponse);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Response> sendResetCode(@RequestBody ForgotPasswordRequest request) {
        try {
            userService.generateAndSendResetCode(request.getEmail());
            return ResponseEntity.ok(Response.successMessage("Reset code sent to email"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.failMessage(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Response> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (!userService.verifyResetCode(request.getEmail(), request.getCode())) {
            return ResponseEntity.badRequest().body(Response.failMessage("Invalid reset code"));
        }
        try {
            userService.resetPassword(request.getEmail(), request.getNewPassword());
            return ResponseEntity.ok(Response.successMessage("Password has been reset"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Response.failMessage(e.getMessage()));
        }
    }
}