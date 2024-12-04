//package uni.backend.controller;
//
//import static org.hamcrest.Matchers.hasSize;
//import static org.hamcrest.Matchers.is;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import uni.backend.domain.Matching;
//import uni.backend.domain.User;
//import uni.backend.service.MatchingService;
//import uni.backend.service.UserService;
//import uni.backend.controller.MatchingController;
//
//@WebMvcTest(MatchingController.class)
//class MatchingControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private MatchingService matchingService;
//
//    @MockBean
//    private UserService userService;
//
//    @MockBean
//    private SimpMessagingTemplate messagingTemplate;
//
//    private Matching matching1;
//    private Matching matching2;
//
//    @BeforeEach
//    void setUp() {
//        User requester = new User();
//        requester.setUserId(1);
//
//        User receiver = new User();
//        receiver.setUserId(2);
//
//        matching1 = Matching.builder()
//                .matchingId(1)
//                .requester(requester)
//                .receiver(receiver)
//                .status(Matching.Status.PENDING)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        matching2 = Matching.builder()
//                .matchingId(2)
//                .requester(requester)
//                .receiver(receiver)
//                .status(Matching.Status.ACCEPTED)
//                .createdAt(LocalDateTime.now())
//                .build();
//    }
//
//    @Test
//    @WithMockUser
//    void 요청자_ID로_매칭_목록_조회() throws Exception {
//        // given
//        when(matchingService.getMatchingListByRequesterId(1)).thenReturn(Arrays.asList(matching1, matching2));
//
//        // when
//        mockMvc.perform(get("/api/match/list/requester/1"))
//
//                // then
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2)))
//                .andExpect(jsonPath("$[0].matchingId", is(1)))
//                .andExpect(jsonPath("$[0].requesterId", is(1)))
//                .andExpect(jsonPath("$[0].status", is("PENDING")));
//    }
//
//    @Test
//    @WithMockUser
//    void 수신자_ID로_매칭_목록_조회() throws Exception {
//        // given
//        when(matchingService.getMatchingListByReceiverId(2)).thenReturn(Arrays.asList(matching1, matching2));
//
//        // when
//        mockMvc.perform(get("/api/match/list/receiver/2"))
//
//                // then
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2)))
//                .andExpect(jsonPath("$[1].matchingId", is(2)))
//                .andExpect(jsonPath("$[1].receiverId", is(2)))
//                .andExpect(jsonPath("$[1].status", is("ACCEPTED")));
//    }
//}
