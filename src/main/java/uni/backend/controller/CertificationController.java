package uni.backend.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uni.backend.repository.UserRepository;
import uni.backend.service.CertificationService;


/*
 * 이메일 중복 확인 및 인증처리 *
 * */


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class CertificationController {

  private final CertificationService certificationService;
  private final UserRepository userRepository;

  // 이메일 인증 및 중복 확인 메서드
  @PostMapping("validate")
  public ResponseEntity<Map<String, String>> validateEmail(
      @RequestBody Map<String, String> request) {
    String email = request.get("email");
    String univName = request.get("univName");

    if (userRepository.existsByEmail(email)) {
      return ResponseEntity.ok(Map.of("status", "fail", "message", "이미 가입된 이메일입니다."));
    }

    boolean isSent = certificationService.requestCertification(email, univName, false);
    if (isSent) {
      return ResponseEntity.ok(Map.of("status", "success"));
    } else {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("status", "fail", "message", "이메일 인증 요청 실패"));
    }

  }


  @PostMapping("/verify")
  public ResponseEntity<Map<String, String>> verifyCertificationCode(
      @RequestBody Map<String, Object> request) {
    String email = (String) request.get("email");
    String univName = (String) request.get("univName");
    int code = (Integer) request.get("code");

    boolean isVerified = certificationService.verifyCertification(email, univName, code);
    if (isVerified) {
      return ResponseEntity.ok(Map.of("status", "success"));
    } else {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("status", "fail", "message", "인증 코드 검증 실패"));
    }
  }


  // 대학교 이름 검증 메서드
  @PostMapping("/univ")
  public ResponseEntity<Map<String, String>> validateUniversity(
      @RequestBody Map<String, String> request) {
    String univName = request.get("univname");

    boolean isValid = certificationService.universityCertification(univName);
    if (isValid) {
      return ResponseEntity.ok(Map.of("status", "success"));
    } else {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("status", "fail", "message", "인증 불가능한 대학교입니다."));
    }
  }
}