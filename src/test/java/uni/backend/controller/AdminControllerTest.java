package uni.backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import uni.backend.config.TestSecurityConfig;
import uni.backend.domain.UserStatus;
import uni.backend.domain.dto.ReportedUserResponse;
import uni.backend.domain.dto.UserResponse;
import uni.backend.security.JwtUtils;
import uni.backend.service.AdminService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(TestSecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("관리자 계정 생성")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldCreateAdminAccount() throws Exception {
        // given
        Mockito.doNothing().when(adminService).createAccount(any());

        // when & then
        mockMvc.perform(post("/api/admin/create-account"))
            .andExpect(status().isOk())
            .andExpect(content().string("관리자 계정이 생성되었으며 이메일이 발송되었습니다."));

        Mockito.verify(adminService).createAccount(any());
    }

    @Test
    @DisplayName("모든 유저 리스트 조회")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldReturnAllUsers() throws Exception {
        // given
        Page<UserResponse> users = new PageImpl<>(List.of());
        Mockito.when(adminService.getAllUsers(any(), any(Integer.class), any(Integer.class)))
            .thenReturn(users);

        // when & then
        mockMvc.perform(get("/api/admin/users")
                .param("status", "ACTIVE")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        Mockito.verify(adminService).getAllUsers(eq(UserStatus.ACTIVE), eq(0), eq(10));
    }

    @Test
    @DisplayName("유저 상태 변경 및 제재 날짜 설정")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldUpdateUserStatus() throws Exception {
        // given
        Mockito.doNothing().when(adminService)
            .updateUserStatus(any(Integer.class), any(UserStatus.class), any(Integer.class));

        // when & then
        mockMvc.perform(patch("/api/admin/users/1/status")
                .param("status", "BANNED")
                .param("banDays", "30"))
            .andExpect(status().isOk())
            .andExpect(content().string("유저 상태가 성공적으로 업데이트되었습니다."));

        Mockito.verify(adminService).updateUserStatus(eq(1), eq(UserStatus.BANNED), eq(30));
    }

    @Test
    @DisplayName("신고된 유저 목록 조회")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldReturnReportedUsers() throws Exception {
        // given
        Page<ReportedUserResponse> reportedUsers = new PageImpl<>(List.of());
        Mockito.when(adminService.getReportedUsers(any(Integer.class), any(Integer.class)))
            .thenReturn(reportedUsers);

        // when & then
        mockMvc.perform(get("/api/admin/reported-users")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        Mockito.verify(adminService).getReportedUsers(eq(1), eq(10));
    }

    @Test
    @DisplayName("유저의 모든 콘텐츠 블라인드 처리")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldBlindAllUserContent() throws Exception {
        // given
        Mockito.doNothing().when(adminService).blindAllContentByUser(any(Integer.class));

        // when & then
        mockMvc.perform(post("/api/admin/users/1/blind-content"))
            .andExpect(status().isOk())
            .andExpect(content().string("유저의 모든 콘텐츠가 블라인드 처리되었습니다."));

        Mockito.verify(adminService).blindAllContentByUser(eq(1));
    }

    @Test
    @DisplayName("유저의 모든 콘텐츠 블라인드 해제")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldUnblindAllUserContent() throws Exception {
        // given
        Mockito.doNothing().when(adminService).unblindAllContentByUser(any(Integer.class));

        // when & then
        mockMvc.perform(post("/api/admin/users/1/unblind-content"))
            .andExpect(status().isOk())
            .andExpect(content().string("유저의 모든 콘텐츠 블라인드 상태가 해제되었습니다."));

        Mockito.verify(adminService).unblindAllContentByUser(eq(1));
    }
}