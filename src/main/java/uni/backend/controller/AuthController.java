package uni.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.User;
import uni.backend.domain.dto.CustomUserDetails;
import uni.backend.domain.dto.LoginRequest;
import uni.backend.repository.UserRepository;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // 프론트엔드 도메인 허용
public class AuthController {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 사용자 인증 토큰 생성
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

            // 인증 시도
            Authentication authentication = authenticationManager.authenticate(authToken);

            // 인증 성공 시 응답
            return ResponseEntity.ok("{\"message\": \"로그인에 성공했습니다.\"}");
        } catch (AuthenticationException e) {
            // 인증 실패 시 응답
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"인증에 실패하였습니다.\"}");
        }
    }

    @GetMapping("/user-role")
    public ResponseEntity<?> getUserRole(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername());

        if (user != null) {
            return ResponseEntity.ok(user.getRole().name());
        } else {
            return ResponseEntity.status(404).body("User not found");
        }
    }
}
