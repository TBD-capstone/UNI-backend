package uni.backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import uni.backend.config.TestSecurityConfig;
import uni.backend.domain.University;
import uni.backend.domain.dto.UniversityResponse;
import uni.backend.security.JwtUtils;
import uni.backend.service.CertificationService;
import uni.backend.service.UniversityService;
import uni.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.util.Map;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CertificationController.class)
@Import(TestSecurityConfig.class)
class CertificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CertificationService certificationService;

    @MockBean
    private UniversityService universityService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("이메일 인증 및 중복 확인 - 성공")
    @WithMockUser(username = "korean", roles = {"KOREAN"})
    void shouldValidateEmailSuccessfully() throws Exception {
        // given
        Mockito.when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        Mockito.when(
                certificationService.requestCertification("test@example.com", "Test University", false))
            .thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"univName\":\"Test University\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"));

        Mockito.verify(userRepository).existsByEmail("test@example.com");
        Mockito.verify(certificationService)
            .requestCertification("test@example.com", "Test University", false);
    }

    @Test
    @DisplayName("이메일 인증 및 중복 확인 - 실패 (중복 이메일)")
    @WithMockUser(username = "korean", roles = {"KOREAN"})
    void shouldFailValidationDueToDuplicateEmail() throws Exception {
        // given
        Mockito.when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"univName\":\"Test University\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("fail"))
            .andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));

        Mockito.verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    @DisplayName("이메일 인증 요청 실패")
    @WithMockUser(username = "korean", roles = {"KOREAN"})
    void shouldFailToSendCertificationEmail() throws Exception {
        // given
        Map<String, String> request = Map.of(
            "email", "test@example.com",
            "univName", "Test University"
        );

        Mockito.when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        Mockito.when(
                certificationService.requestCertification("test@example.com", "Test University", false))
            .thenReturn(false); // 인증 요청 실패를 모킹

        // when & then
        mockMvc.perform(post("/api/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest()) // 400 상태 코드 확인
            .andExpect(jsonPath("$.status").value("fail"))
            .andExpect(jsonPath("$.message").value("이메일 인증 요청 실패"));

        Mockito.verify(userRepository).existsByEmail("test@example.com");
        Mockito.verify(certificationService)
            .requestCertification("test@example.com", "Test University", false);
    }

    @Test
    @DisplayName("인증 코드 검증 - 성공")
    @WithMockUser(username = "korean", roles = {"KOREAN"})
    void shouldVerifyCertificationCodeSuccessfully() throws Exception {
        // given
        Mockito.when(
                certificationService.verifyCertification("test@example.com", "Test University", 1234))
            .thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/auth/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"email\":\"test@example.com\",\"univName\":\"Test University\",\"code\":1234}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"));

        Mockito.verify(certificationService)
            .verifyCertification("test@example.com", "Test University", 1234);
    }

    @Test
    @DisplayName("인증 코드 검증 - 실패")
    @WithMockUser(username = "korean", roles = {"KOREAN"})
    void shouldFailToVerifyCertificationCode() throws Exception {
        // given
        Mockito.when(
                certificationService.verifyCertification("test@example.com", "Test University", 1234))
            .thenReturn(false);

        // when & then
        mockMvc.perform(post("/api/auth/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"email\":\"test@example.com\",\"univName\":\"Test University\",\"code\":1234}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("fail"))
            .andExpect(jsonPath("$.message").value("인증 코드 검증 실패"));

        Mockito.verify(certificationService)
            .verifyCertification("test@example.com", "Test University", 1234);
    }

    @Test
    @DisplayName("대학교 목록 조회")
    @WithMockUser(username = "korean", roles = {"KOREAN"})
    void shouldReturnAllUniversities() throws Exception {
        // given
        University university = new University();
        university.setUniversityId(1);
        university.setUniName("Test University");

        List<University> universities = List.of(university);
        Mockito.when(universityService.findAll()).thenReturn(universities);

        // when & then
        mockMvc.perform(get("/api/auth/univ"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].universityId").value(1))
            .andExpect(jsonPath("$[0].univName").value("Test University"));

        Mockito.verify(universityService).findAll();
    }

    @Test
    @DisplayName("대학교 이름 검증 - 성공")
    @WithMockUser(username = "korean", roles = {"KOREAN"})
    void shouldValidateUniversitySuccessfully() throws Exception {
        // given
        Mockito.when(certificationService.universityCertification("Test University"))
            .thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/auth/univ")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"univName\":\"Test University\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"));

        Mockito.verify(certificationService).universityCertification("Test University");
    }

    @Test
    @DisplayName("대학교 이름 검증 - 실패")
    @WithMockUser(username = "korean", roles = {"KOREAN"})
    void shouldFailToValidateUniversity() throws Exception {
        // given
        Mockito.when(certificationService.universityCertification("Test University"))
            .thenReturn(false);

        // when & then
        mockMvc.perform(post("/api/auth/univ")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"univName\":\"Test University\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("fail"))
            .andExpect(jsonPath("$.message").value("인증 불가능한 대학교입니다."));

        Mockito.verify(certificationService).universityCertification("Test University");
    }

    @Test
    @DisplayName("인증된 유저 리스트 가져오기 성공")
    @WithMockUser(username = "korean", roles = {"KOREAN"})
    void shouldReturnCertifiedUserListSuccessfully() throws Exception {
        // given
        String key = "test-key";
        List<Map<String, Object>> certifiedUsers = List.of(
            Map.of("id", 1, "name", "John Doe"),
            Map.of("id", 2, "name", "Jane Doe")
        );

        Mockito.when(certificationService.getCertifiedUserList(key)).thenReturn(certifiedUsers);

        Map<String, String> request = Map.of("key", key);

        // when & then
        mockMvc.perform(post("/api/auth/UserList")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].id").value(1))
            .andExpect(jsonPath("$.data[0].name").value("John Doe"))
            .andExpect(jsonPath("$.data[1].id").value(2))
            .andExpect(jsonPath("$.data[1].name").value("Jane Doe"));

        Mockito.verify(certificationService).getCertifiedUserList(key);
    }

    @Test
    @DisplayName("인증된 유저 리스트 가져오기 실패")
    @WithMockUser(username = "korean", roles = {"KOREAN"})
    void shouldFailToReturnCertifiedUserList() throws Exception {
        // given
        String key = "invalid-key";

        Mockito.when(certificationService.getCertifiedUserList(key))
            .thenThrow(new RuntimeException("인증된 유저 리스트 가져오기 실패"));

        Map<String, String> request = Map.of("key", key);

        // when & then
        mockMvc.perform(post("/api/auth/UserList")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("fail"))
            .andExpect(jsonPath("$.message").value("인증된 유저 리스트 가져오기 실패"));

        Mockito.verify(certificationService).getCertifiedUserList(key);
    }

    @Test
    @DisplayName("인증된 유저 목록 초기화 - 성공")
    @WithMockUser(username = "korean", roles = {"KOREAN"})
    void shouldClearCertifiedUsersSuccessfully() throws Exception {
        // given
        Mockito.when(certificationService.clearCertifiedUsers()).thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/auth/clear"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.message").value("인증된 유저 목록이 초기화되었습니다."));

        Mockito.verify(certificationService).clearCertifiedUsers();
    }

    @Test
    @DisplayName("인증된 유저 목록 초기화 - 실패")
    @WithMockUser(username = "korean", roles = {"KOREAN"})
    void shouldFailToClearCertifiedUsers() throws Exception {
        // given
        Mockito.when(certificationService.clearCertifiedUsers()).thenReturn(false);

        // when & then
        mockMvc.perform(post("/api/auth/clear"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("fail"))
            .andExpect(jsonPath("$.message").value("인증된 유저 목록 초기화 실패"));

        Mockito.verify(certificationService).clearCertifiedUsers();
    }
}