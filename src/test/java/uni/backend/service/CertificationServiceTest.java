package uni.backend.service;

import java.util.List;
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
        mockResponse.put("success", true);

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

    @Test
    void 인증코드_검증_성공() {
        // Given
        String email = "test@example.com";
        String univName = "Sample University";
        int code = 123456;

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true); // 성공 응답

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        boolean result = certificationService.verifyCertification(email, univName, code);

        // Then
        assertTrue(result, "인증 코드 검증이 성공해야 합니다.");
    }

    @Test
    void 인증코드_검증_실패() {
        String email = "test@example.com";
        String univName = "Sample University";
        int code = 123456;

        // Given
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", false);

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When

        boolean result = certificationService.verifyCertification(email, univName, code);
        // Then
        assertFalse(result, "인증 코드 검증이 실패해야 합니다.");

    }

    @Test
    void 대학_인증_성공() {
        // Given
        String univName = "Sample University";

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true); // 성공 응답

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        boolean result = certificationService.universityCertification(univName);

        // Then
        assertTrue(result, "대학교 인증이 성공해야 합니다.");
    }

    @Test
    void 대학_인증_실패() {
        // Given
        String univName = "Unknown University";

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", false); // 실패 응답

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        boolean result = certificationService.universityCertification(univName);

        // Then
        assertFalse(result, "대학교 인증이 실패해야 합니다.");
    }

    @Test
    void 인증된_유저_리스트_가져오기_성공() {
        // Given
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("data", List.of(
            Map.of("email", "test1@example.com", "univName", "Sample University 1"),
            Map.of("email", "test2@example.com", "univName", "Sample University 2")
        ));

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        List<Map<String, Object>> result = certificationService.getCertifiedUserList("key");

        // Then
        assertNotNull(result, "결과가 null이 아니어야 합니다.");
        assertEquals(2, result.size(), "인증된 유저 리스트의 크기가 2여야 합니다.");
        verify(restTemplate).postForEntity(anyString(), any(), eq(Map.class));
    }

    @Test
    void 인증된_유저_리스트_가져오기_실패() {
        // Given
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", false);

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        List<Map<String, Object>> result = certificationService.getCertifiedUserList("key");

        // Then
        assertNotNull(result, "결과가 null이 아니어야 합니다.");
        assertEquals(0, result.size(), "인증된 유저 리스트의 크기가 0이어야 합니다.");
        verify(restTemplate).postForEntity(anyString(), any(), eq(Map.class));
    }

    @Test
    void 인증된_유저_목록_초기화_성공() {
        // Given
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        boolean result = certificationService.clearCertifiedUsers();

        // Then
        assertTrue(result, "유저 목록 초기화가 성공해야 합니다.");
        verify(restTemplate).postForEntity(anyString(), any(), eq(Map.class));
    }

    @Test
    void 인증된_유저_목록_초기화_실패() {
        // Given
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", false);

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        boolean result = certificationService.clearCertifiedUsers();

        // Then
        assertFalse(result, "유저 목록 초기화가 실패해야 합니다.");
        verify(restTemplate).postForEntity(anyString(), any(), eq(Map.class));
    }

}

