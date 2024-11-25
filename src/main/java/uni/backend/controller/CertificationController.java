package uni.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uni.backend.domain.University;
import uni.backend.domain.dto.UniversityResponse;
import uni.backend.repository.UserRepository;
import uni.backend.service.CertificationService;
import uni.backend.service.UniversityService;


/*
 * 이메일 중복 확인 및 인증처리 *
 * */


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class CertificationController {


    private final UniversityService universityService;
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

    //대학 리스트 받아오기

    @GetMapping("/univ")
    public List<UniversityResponse> getAllUniversities() {
        List<University> universities = universityService.findAll();
        return universities.stream()
            .map(university -> {
                UniversityResponse dto = new UniversityResponse();
                dto.setUniversityId(university.getUniversityId());
                dto.setUnivName(university.getUniName());
                return dto;
            })
            .collect(Collectors.toList());
    }

    // 대학교 이름 검증 메서드
    @PostMapping("/univ")
    public ResponseEntity<Map<String, String>> validateUniversity(
        @RequestBody Map<String, String> request) {
        String univName = request.get("univName");

        boolean isValid = certificationService.universityCertification(univName);
        if (isValid) {
            return ResponseEntity.ok(Map.of("status", "success"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("status", "fail", "message", "인증 불가능한 대학교입니다."));
        }
    }


    @PostMapping("/UserList")
    public ResponseEntity<?> getCertifiedUserList(@RequestBody Map<String, String> request) {
        String key = request.get("key");

        try {
            List<Map<String, Object>> certifiedUsers = certificationService.getCertifiedUserList(
                key);
            return ResponseEntity.ok(Map.of("status", "success", "data", certifiedUsers));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("status", "fail", "message", "인증된 유저 리스트 가져오기 실패"));
        }
    }


    // 인증된 유저 목록 초기화 엔드포인트
    @PostMapping("/clear")
    public ResponseEntity<Map<String, String>> clearCertifiedUsers() {
        boolean isCleared = certificationService.clearCertifiedUsers();

        if (isCleared) {
            return ResponseEntity.ok(
                Map.of("status", "success", "message", "인증된 유저 목록이 초기화되었습니다."));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("status", "fail", "message", "인증된 유저 목록 초기화 실패"));
        }
    }

}