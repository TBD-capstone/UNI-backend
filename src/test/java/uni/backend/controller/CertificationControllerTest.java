package uni.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uni.backend.domain.dto.UniversityResponse;
import uni.backend.repository.UserRepository;
import uni.backend.security.JwtUtils;
import uni.backend.service.CertificationService;
import uni.backend.service.UniversityService;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CertificationController.class)
@Import(TestSecurityConfig.class)
public class CertificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CertificationService certificationService;

    @MockBean
    private UniversityService universityService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtils jwtUtils; // MockBean 추가

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("이메일 인증 및 중복 확인 - 성공")
    @WithMockUser(username = "user", roles = {"USER"})
    void validateEmail_Success() throws Exception {
        when(universityService.convertToKorean("Test University")).thenReturn("테스트 대학교");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(certificationService.requestCertification("test@example.com", "테스트 대학교",
            false)).thenReturn(true);

        mockMvc.perform(post("/api/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    Map.of("email", "test@example.com", "univName", "Test University"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("이메일 인증 요청 실패")
    @WithMockUser(username = "user", roles = {"USER"})
    void validateEmail_Fail() throws Exception {
        when(universityService.convertToKorean("Test University")).thenReturn("테스트 대학교");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(certificationService.requestCertification("test@example.com", "테스트 대학교",
            false)).thenReturn(false);

        mockMvc.perform(post("/api/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    Map.of("email", "test@example.com", "univName", "Test University"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("fail"))
            .andExpect(jsonPath("$.message").value("이메일 인증 요청 실패"));
    }

    @Test
    @DisplayName("인증 코드 검증 - 성공")
    @WithMockUser(username = "user", roles = {"USER"})
    void verifyCertificationCode_Success() throws Exception {
        when(universityService.convertToKorean("Test University")).thenReturn("테스트 대학교");
        when(certificationService.verifyCertification("test@example.com", "테스트 대학교",
            1234)).thenReturn(true);

        mockMvc.perform(post("/api/auth/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    Map.of("email", "test@example.com", "univName", "Test University", "code", 1234))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("대학교 인증 - 성공")
    @WithMockUser(username = "user", roles = {"USER"})
    void validateUniversity_Success() throws Exception {
        when(certificationService.universityCertification("Test University")).thenReturn(true);

        mockMvc.perform(post("/api/auth/univ")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("univName", "Test University"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("대학교 목록 조회")
    @WithMockUser(username = "user", roles = {"USER"})
    void getUniversityList() throws Exception {
        when(universityService.getUniversities()).thenReturn(List.of(
            UniversityResponse.builder()
                .universityId(1)
                .univName("Test University")
                .build()
        ));

        mockMvc.perform(get("/api/auth/univ"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].universityId").value(1))
            .andExpect(jsonPath("$[0].univName").value("Test University"));
    }

    @Test
    @DisplayName("인증된 유저 목록 초기화 - 성공")
    @WithMockUser(username = "user", roles = {"USER"})
    void clearCertifiedUsers_Success() throws Exception {
        when(certificationService.clearCertifiedUsers()).thenReturn(true);

        mockMvc.perform(post("/api/auth/clear"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.message").value("인증된 유저 목록이 초기화되었습니다."));
    }

    @Test
    @DisplayName("이메일 인증 및 중복 확인 - 이미 가입된 이메일")
    @WithMockUser(username = "user", roles = {"USER"})
    void validateEmail_AlreadyRegistered() throws Exception {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    Map.of("email", "test@example.com", "univName", "Test University"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("fail"))
            .andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));
    }


    @Test
    @DisplayName("인증 코드 검증 - 실패")
    @WithMockUser(username = "user", roles = {"USER"})
    void verifyCertificationCode_Fail() throws Exception {
        when(universityService.convertToKorean("Test University")).thenReturn("테스트 대학교");
        when(certificationService.verifyCertification("test@example.com", "테스트 대학교",
            1234)).thenReturn(false);

        mockMvc.perform(post("/api/auth/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    Map.of("email", "test@example.com", "univName", "Test University", "code", 1234))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("fail"))
            .andExpect(jsonPath("$.message").value("인증 코드 검증 실패"));
    }

    @Test
    @DisplayName("대학교 인증 - 실패")
    @WithMockUser(username = "user", roles = {"USER"})
    void validateUniversity_Fail() throws Exception {
        when(certificationService.universityCertification("Invalid University")).thenReturn(false);

        mockMvc.perform(post("/api/auth/univ")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("univName", "Invalid University"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("fail"))
            .andExpect(jsonPath("$.message").value("인증 불가능한 대학교입니다."));
    }

    @Test
    @DisplayName("인증된 유저 목록 초기화 - 실패")
    @WithMockUser(username = "user", roles = {"USER"})
    void clearCertifiedUsers_Fail() throws Exception {
        when(certificationService.clearCertifiedUsers()).thenReturn(false);

        mockMvc.perform(post("/api/auth/clear"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("fail"))
            .andExpect(jsonPath("$.message").value("인증된 유저 목록 초기화 실패"));
    }


}
