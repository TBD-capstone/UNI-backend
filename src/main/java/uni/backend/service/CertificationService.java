package uni.backend.service;

import com.univcert.api.UnivCert;
import java.security.PublicKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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

  //  @Value("${univCert.key}")
  private static final String API_KEY = "0a44d2bc-1cae-4991-a5e1-41884894b333"; // 부여받은 API Key

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
      ResponseEntity<Map> response = restTemplate.postForEntity(CERTIFY_CODE_API_URL, requestBody,
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

}
