package uni.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import uni.backend.service.CertificationService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CertificationServiceTest {


  //  CertificationService는 실제 RestTemplate이 아닌, 테스트 환경에서 Mock 객체로 동작
  @InjectMocks
  private CertificationService certificationService;


  // RestTemplate을 Mock으로 설정하여 실제 API 호출을 막고, 원하는 동작을 모의
  @Mock
  private RestTemplate restTemplate;

  //@Mock과 @InjectMocks가 선언된 필드들을 초기화하여 사용할 수 있도록 함
  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void 인증요청_성공() {
    // 테스트용 데이터 설정

    String email = "test@example.com";
    String univName = "Sample University";
    boolean univCheck = true;

    // Given
    Map<String, Object> mockResponse = new HashMap<>();
    mockResponse.put("status", true);

    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

    // When: 인증 요청 메서드 실행
    boolean result = certificationService.requestCertification(email, univName, univCheck);

    // Then: 결과 검증
    assertTrue(result, "인증 요청이 성공해야 합니다.");
  }

  @Test
  void 인증요청_실패() {
    String email = "test@example.com";
    String univName = "Sample University";
    boolean univCheck = true;

    // Given
    Map<String, Object> mockResponse = new HashMap<>();
    mockResponse.put("success", false);

    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

    // When
    boolean result = certificationService.requestCertification(email, univName, univCheck);
    //then
    assertFalse(result, "인증 요청이 실패해야 합니다.");
  }


}

