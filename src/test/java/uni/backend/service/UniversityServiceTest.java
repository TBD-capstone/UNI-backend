package uni.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uni.backend.domain.University;
import uni.backend.domain.dto.UniversityResponse;
import uni.backend.repository.UniversityRepository;

public class UniversityServiceTest {

    @InjectMocks
    private UniversityService universityService; // 테스트 대상

    @Mock
    private UniversityRepository universityRepository; // Mock 객체

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mock 객체 초기화
    }

    @Test
    void 모든대학정보조회_빈데이터() {
        // Given: 빈 리스트 설정
        when(universityRepository.findAll()).thenReturn(Collections.emptyList());

        // When: 메서드 실행
        List<University> result = universityService.findAll();

        // Then: 결과 검증
        assertNotNull(result, "결과는 null이 아니어야 합니다.");
        assertEquals(0, result.size(), "반환된 대학의 수는 0이어야 합니다.");
    }

    @Test
    void getUniversities_데이터조회성공() {
        // Given: Mock 데이터 설정
        University university1 = new University(1, "한국대학교", "Korea University");
        University university2 = new University(2, "서울대학교", "Seoul National University");
        when(universityRepository.findAll()).thenReturn(List.of(university1, university2));

        // When: 메서드 실행
        List<UniversityResponse> responses = universityService.getUniversities();

        // Then: 결과 검증
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Korea University", responses.get(0).getEnUnivName());
        assertEquals("서울대학교", responses.get(1).getUnivName());
    }

    @Test
    void convertToKorean_영어이름성공() {
        // Given: Mock 데이터 설정
        University mockUniversity = new University(1, "서울대학교", "Seoul National University");
        when(universityRepository.findByEnUniName("Seoul National University")).thenReturn(
            mockUniversity);

        // When: 메서드 실행
        String result = universityService.convertToKorean("Seoul National University");

        // Then: 결과 검증
        assertEquals("서울대학교", result, "변환된 한국어 이름이 맞아야 합니다.");
    }

    @Test
    void convertToKorean_한국어이름_입력그대로반환() {
        // Given: Mock 데이터 없음

        // When: 메서드 실행
        String result = universityService.convertToKorean("서울대학교");

        // Then: 결과 검증
        assertEquals("서울대학교", result, "입력된 한국어 이름이 그대로 반환되어야 합니다.");
    }

    @Test
    void convertToKorean_영어이름_예외처리() {
        // Given: Mock 데이터 설정
        when(universityRepository.findByEnUniName("Nonexistent University")).thenReturn(null);

        // When & Then: 메서드 실행 및 예외 검증
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            universityService.convertToKorean("Nonexistent University");
        });
        assertEquals("University not found with English name: Nonexistent University",
            exception.getMessage());
    }

    @Test
    void isEnglish_영문문자열_참() {
        // Given: 영문 문자열 입력
        String input = "English Text";

        // When: 메서드 실행
        boolean result = universityService.isEnglish(input);

        // Then: 결과 검증
        assertTrue(result, "영문 문자열은 true여야 합니다.");
    }

    @Test
    void isEnglish_한글문자열_거짓() {
        // Given: 한글 문자열 입력
        String input = "한국어 텍스트";

        // When: 메서드 실행
        boolean result = universityService.isEnglish(input);

        // Then: 결과 검증
        assertFalse(result, "한글 문자열은 false여야 합니다.");
    }

    @Test
    void isEnglish_혼합문자열_거짓() {
        // Given: 혼합 문자열 입력
        String input = "English 123 한글";

        // When: 메서드 실행
        boolean result = universityService.isEnglish(input);

        // Then: 결과 검증
        assertFalse(result, "혼합 문자열은 false여야 합니다.");
    }
}
