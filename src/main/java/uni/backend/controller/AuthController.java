package uni.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.Role;
import uni.backend.domain.User;
import uni.backend.domain.dto.*;
import uni.backend.repository.UserRepository;
import uni.backend.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    @Autowired
    private final UserService userService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // SecurityConfig에 있는 passwordEncoder를 가져와야 함

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
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            // 인증 토큰 생성
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

            // 인증 시도
            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 세션 생성 및 SecurityContext 설정
            request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // 인증된 사용자 정보 가져오기
            User user = (User) authentication.getPrincipal();

            // 성공 응답 전송
            return ResponseEntity.ok(new LoginResponse("success", "logged in successfully",
                    user.getName(), user.getUserId(), user.getRole() == Role.KOREAN));
        } catch (Exception e) {
            // 실패 응답 전송
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response("fail", "wrong information"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Response> logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
            return ResponseEntity.ok(Response.successMessage("logged out successfully"));
        } else {
            return ResponseEntity.status(400).body(Response.failMessage("logout failed"));
        }
    }

    @GetMapping("/loginCheck")
    public ResponseEntity<?> loginCheck(@AuthenticationPrincipal User user) {
        if (user == null) { // 인증되지 않은 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not logged in.");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getUserId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole() != null ? user.getRole().toString() : "ROLE_NOT_SET");
        return ResponseEntity.ok(response);
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
