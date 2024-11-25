package uni.backend.service;

import com.univcert.api.UnivCert;
import java.security.PublicKey;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CertificationService {

    private final RestTemplate restTemplate;
    private static final String CERTIFY_UNIV_URL = "https://univcert.com/api/v1/check";
    private static final String CERTIFY_API_URL = "https://univcert.com/api/v1/certify";
    private static final String CERTIFY_CODE_API_URL = "https://univcert.com/api/v1/certifycode";
    private static final String CERTIFY_USER_LIST_URL = "https://univcert.com/api/v1/certifiedlist";
    private static final String CLEAR_CERTIFIED_USERS_URL = "https://univcert.com/api/v1/clear";

    @Value("${univCert.key}")
    private String API_KEY;

    // 1 인증 코드 요청 메서드
    public boolean requestCertification(String email, String univName, boolean univCheck) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("key", API_KEY);
        requestBody.put("email", email);
        requestBody.put("univName", univName);
        requestBody.put("univ_check", univCheck);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(CERTIFY_API_URL, requestBody,
                Map.class);
            return Boolean.TRUE.equals(response.getBody().get("success"));
        } catch (HttpClientErrorException ex) {
            System.out.println("인증 코드 발송 실패: " + ex.getResponseBodyAsString());
        }
        return false;
    }

    // 2 인증 코드 검증 메서드
    public boolean verifyCertification(String email, String univName, int code) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("key", API_KEY);
        requestBody.put("email", email);
        requestBody.put("univName", univName);
        requestBody.put("code", code);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(CERTIFY_CODE_API_URL,
                requestBody,
                Map.class);
            return Boolean.TRUE.equals(response.getBody().get("success"));
        } catch (HttpClientErrorException ex) {
            System.out.println("인증 코드 검증 실패: " + ex.getResponseBodyAsString());
        }
        return false;
    }

    public boolean universityCertification(String univName) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("univName", univName);

        try {
            // 인증 가능한 대학교인지 확인하는 API 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(CERTIFY_UNIV_URL, requestBody,
                Map.class);

            // 응답 본문에서 "success" 값을 확인하여 인증 가능 여부를 반환
            Map<String, Object> responseBody = response.getBody();
            return Boolean.TRUE.equals(responseBody.get("success"));
        } catch (HttpClientErrorException ex) {
            System.out.println("대학교 인증 실패: " + ex.getResponseBodyAsString());
            return false;
        }
    }

    // 4 인증된 유저 리스트 조회 메서드

    public List<Map<String, Object>> getCertifiedUserList(String key) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("key", API_KEY);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(CERTIFY_USER_LIST_URL,
                requestBody, Map.class);
            if (Boolean.TRUE.equals(response.getBody().get("success"))) {
                return (List<Map<String, Object>>) response.getBody().get("data");
            }
        } catch (HttpClientErrorException ex) {
            System.out.println("인증된 유저 리스트 가져오기 실패: " + ex.getResponseBodyAsString());
        }
        return List.of();
    }

    // 인증된 유저 목록 초기화 메서드
    public boolean clearCertifiedUsers() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("key", API_KEY);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                CLEAR_CERTIFIED_USERS_URL, requestBody, Map.class);
            return Boolean.TRUE.equals(response.getBody().get("success"));
        } catch (HttpClientErrorException ex) {
            System.out.println("인증된 유저 목록 초기화 실패: " + ex.getResponseBodyAsString());
            return false;
        }
    }


}

