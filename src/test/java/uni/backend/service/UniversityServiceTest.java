package uni.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uni.backend.domain.University;
import uni.backend.repository.UniversityRepository;

public class UniversityServiceTest {

  @InjectMocks
  private UniversityService universityService;

  @Mock
  private UniversityRepository universityRepository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }


  @Test
  void 모든대학정보조회() {
    // Given: Mock 데이터 설정
    University university1 = new University(1, "Sample University 1");
    University university2 = new University(2, "Sample University 2");
    List<University> mockUniversities = Arrays.asList(university1, university2);

    when(universityRepository.findAll()).thenReturn(mockUniversities);

    // When: 메서드 실행
    List<University> result = universityService.findAll();

    // Then: 결과 검증
    assertNotNull(result, "결과는 null이 아니어야 합니다.");
    assertEquals(2, result.size(), "반환된 대학의 수가 같아야 합니다.");
    assertEquals("Sample University 1", result.get(0).getUniName(), "첫 번째 대학의 이름이 같아야 합니다.");
    assertEquals("Sample University 2", result.get(1).getUniName(), "두 번째 대학의 이름이 같아야 합니다.");
  }


}
