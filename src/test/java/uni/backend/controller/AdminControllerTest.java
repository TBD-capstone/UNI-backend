//package uni.backend.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import uni.backend.domain.UserStatus;
//import uni.backend.domain.dto.ReportedUserResponse;
//import uni.backend.domain.dto.UserResponse;
//import uni.backend.service.AdminService;
//
//import java.util.List;
//
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(AdminController.class)
//public class AdminControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private AdminService adminService;
//
//    @BeforeEach
//    void setUp() {
//        // 필요한 설정이나 초기화 작업을 이곳에 작성합니다.
//    }
//
//    @Test
//    void testCreateAdminAccount() throws Exception {
//        // given
//        List<String> recipientEmails = List.of(
//            "gbe0808@ajou.ac.kr",
//            "uko802@ajou.ac.kr",
//            "ljy9085@ajou.ac.kr",
//            "han1267@ajou.ac.kr",
//            "nicola1928@ajou.ac.kr"
//        );
//
//        // when
//        mockMvc.perform(post("/api/admin/create-account")
//                .contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andExpect(content().string("관리자 계정이 생성되었으며 이메일이 발송되었습니다."));
//
//        // then
//        verify(adminService, times(1)).createAccount(recipientEmails);
//    }
//
//    @Test
//    void testGetAllUsers() throws Exception {
//        // given
//        Page<UserResponse> users = mock(Page.class);
//        when(adminService.getAllUsers(any(), anyInt(), anyInt())).thenReturn(users);
//
//        // when
//        mockMvc.perform(get("/api/admin/users")
//                .param("page", "0")
//                .param("size", "10"))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.content").isArray());
//
//        // then
//        verify(adminService, times(1)).getAllUsers(any(), eq(0), eq(10));
//    }
//
//    @Test
//    void testUpdateUserStatus() throws Exception {
//        // given
//        Integer userId = 1;
//        UserStatus status = UserStatus.BANNED;
//        Integer banDays = 30;
//
//        // when
//        mockMvc.perform(patch("/api/admin/users/{userId}/status", userId)
//                .param("status", status.name())
//                .param("banDays", banDays.toString())
//                .contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andExpect(content().string("유저 상태가 성공적으로 업데이트되었습니다."));
//
//        // then
//        verify(adminService, times(1)).updateUserStatus(userId, status, banDays);
//    }
//
//    @Test
//    void testGetReportedUsers() throws Exception {
//        // given
//        Page<ReportedUserResponse> reportedUsers = mock(Page.class);
//        when(adminService.getReportedUsers(anyInt(), anyInt())).thenReturn(reportedUsers);
//
//        // when
//        mockMvc.perform(get("/api/admin/reported-users")
//                .param("page", "1")
//                .param("size", "10"))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.content").isArray());
//
//        // then
//        verify(adminService, times(1)).getReportedUsers(eq(1), eq(10));
//    }
//
//    @Test
//    void testBlindAllUserContent() throws Exception {
//        // given
//        Integer userId = 1;
//
//        // when
//        mockMvc.perform(post("/api/admin/users/{userId}/blind-content", userId)
//                .contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andExpect(content().string("유저의 모든 콘텐츠가 블라인드 처리되었습니다."));
//
//        // then
//        verify(adminService, times(1)).blindAllContentByUser(userId);
//    }
//
//    @Test
//    void testUnblindAllUserContent() throws Exception {
//        // given
//        Integer userId = 1;
//
//        // when
//        mockMvc.perform(post("/api/admin/users/{userId}/unblind-content", userId)
//                .contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andExpect(content().string("유저의 모든 콘텐츠 블라인드 상태가 해제되었습니다."));
//
//        // then
//        verify(adminService, times(1)).unblindAllContentByUser(userId);
//    }
//}
