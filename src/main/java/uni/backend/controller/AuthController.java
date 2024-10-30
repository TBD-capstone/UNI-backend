package uni.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.Role;
import uni.backend.domain.User;
import uni.backend.domain.dto.CustomUserDetails;
import uni.backend.domain.dto.LoginRequest;
import uni.backend.domain.dto.Response;
import uni.backend.domain.dto.SignupRequest;
import uni.backend.repository.UserRepository;
import uni.backend.service.UserService;
import uni.backend.service.UserServiceImpl;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // 프론트엔드 도메인 허용
public class AuthController {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    @Autowired
    private final UserService userService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // SecurityConfig에 있는 passwordEncoder를 가져와야 함

    @PostMapping("/auth/signup")
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

    @GetMapping("/auth/loginCheck")
    public ResponseEntity<String> loginCheck(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            String responseMessage = String.format("User is logged in: %s", userDetails.getUsername());
            return ResponseEntity.ok(responseMessage);
        } else {
            return ResponseEntity.status(401).body("User is not logged in.");
        }
    }

//    @PostMapping("/login")
//    public ResponseEntity<Response> login(@RequestBody LoginRequest loginRequest) {
//        // 사용자 인증 토큰 생성
//        UsernamePasswordAuthenticationToken authToken =
//                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
//
//        // 인증 시도
//        Authentication authentication = authenticationManager.authenticate(authToken);
//
//        if (authentication.isAuthenticated()) {
//            Response response = Response.successMessage("signed up successfully");
//            return ResponseEntity.ok(response);
//        }
//        Response response = Response.failMessage("signed up failed");
//        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
//    }


//    @GetMapping("/user-role")
//    public ResponseEntity<?> getUserRole(Authentication authentication) {
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return ResponseEntity.status(401).body("Unauthorized");
//        }
//
//        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//        User user = userRepository.findByEmail(userDetails.getUsername());
//
//        if (user != null) {
//            return ResponseEntity.ok(user.getRole().name());
//        } else {
//            return ResponseEntity.status(404).body("User not found");
//        }
//    }
}
