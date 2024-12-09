package uni.backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import uni.backend.config.TestSecurityConfig;
import uni.backend.controller.ChatController;
import uni.backend.domain.dto.ChatMessageRequest;
import uni.backend.domain.dto.ChatMessageResponse;
import uni.backend.domain.dto.ChatRoomRequest;
import uni.backend.domain.dto.ChatRoomResponse;
import uni.backend.security.JwtUtils;
import uni.backend.service.ChatService;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
@Import(TestSecurityConfig.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private SimpMessageSendingOperations messagingTemplate;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("GET /api/chat/rooms - 채팅방 목록 조회")
    @WithMockUser(username = "testUser", roles = {"USER"})
    void getChatRooms() throws Exception {
        // Given
        List<ChatRoomResponse> mockChatRooms = List.of(
                ChatRoomResponse.builder()
                        .chatRoomId(1)
                        .myId(100)
                        .myName("User1")
                        .otherId(200)
                        .otherName("User2")
                        .unreadCount(2)
                        .build(),
                ChatRoomResponse.builder()
                        .chatRoomId(2)
                        .myId(100)
                        .myName("User1")
                        .otherId(201)
                        .otherName("User3")
                        .unreadCount(0)
                        .build()
        );
        when(chatService.getChatRoomsForUser("testUser")).thenReturn(mockChatRooms);

        // When & Then
        mockMvc.perform(get("/api/chat/rooms").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].chatRoomId").value(1))
                .andExpect(jsonPath("$[0].myName").value("User1"))
                .andExpect(jsonPath("$[1].chatRoomId").value(2))
                .andExpect(jsonPath("$[1].myName").value("User1"));
    }

    @Test
    @DisplayName("POST /api/chat/request - 채팅방 생성")
    @WithMockUser(username = "testUser", roles = {"USER"})
    void createChatRoom() throws Exception {
        // Given
        ChatRoomRequest request = ChatRoomRequest.builder()
                .receiverId(200)
                .build();

        ChatRoomResponse response = ChatRoomResponse.builder()
                .chatRoomId(1)
                .myId(100)
                .myName("testUser")
                .otherId(200)
                .otherName("User2")
                .unreadCount(0)
                .build();

        when(chatService.createChatRoom(eq("testUser"), any(ChatRoomRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/chat/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receiverId\":200}")
                        .principal(() -> "testUser")) // Provide Principal explicitly
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chatRoomId").value(1))
                .andExpect(jsonPath("$.myId").value(100))
                .andExpect(jsonPath("$.myName").value("testUser"))
                .andExpect(jsonPath("$.otherId").value(200))
                .andExpect(jsonPath("$.otherName").value("User2"))
                .andExpect(jsonPath("$.unreadCount").value(0));
    }

    @Test
    @DisplayName("GET /api/chat/room/{roomId} - 채팅방 메시지 조회")
    @WithMockUser(username = "testUser", roles = {"USER"})
    void getChatRoomMessages() throws Exception {
        // Given
        List<ChatMessageResponse> mockMessages = List.of(
                ChatMessageResponse.builder()
                        .messageId(1)
                        .roomId(1)
                        .content("Hello")
                        .senderId(100)
                        .receiverId(200)
                        .sendAt(LocalDateTime.now())
                        .build(),
                ChatMessageResponse.builder()
                        .messageId(2)
                        .roomId(1)
                        .content("Hi")
                        .senderId(101)
                        .receiverId(100)
                        .sendAt(LocalDateTime.now())
                        .build()
        );
        when(chatService.getChatMessages(1)).thenReturn(mockMessages);

        // When & Then
        mockMvc.perform(get("/api/chat/room/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].messageId").value(1))
                .andExpect(jsonPath("$[0].content").value("Hello"));
    }

    @Test
    @DisplayName("POST /api/chat/room/{roomId}/messages - 메시지 전송")
    @WithMockUser(username = "testUser", roles = {"USER"})
    void sendRestMessage() throws Exception {
        // Given
        ChatMessageRequest request = ChatMessageRequest.builder()
                .roomId(1)
                .content("Hello")
                .receiverId(200)
                .build();
        ChatMessageResponse response = ChatMessageResponse.builder()
                .messageId(1)
                .roomId(1)
                .content("Hello")
                .senderId(100)
                .receiverId(200)
                .sendAt(LocalDateTime.now())
                .build();
        when(chatService.sendMessage(any(ChatMessageRequest.class), eq("testUser"), eq(1)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/chat/room/1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomId\":1,\"content\":\"Hello\",\"receiverId\":200}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.messageId").value(1))
                .andExpect(jsonPath("$.content").value("Hello"));
    }

    @Test
    @DisplayName("GET /api/chat/translate/{messageId} - 메시지 번역")
    @WithMockUser
    void translateChatMessage() throws Exception {
        // Given
        when(chatService.translateMessage(1, "en")).thenReturn("Translated Message");

        // When & Then
        mockMvc.perform(get("/api/chat/translate/1")
                        .header("Accept-Language", "en")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Translated Message"));
    }

    @Test
    @DisplayName("GET /api/chat/translate/{messageId} - 메시지 번역 실패")
    @WithMockUser
    void translateChatMessageFailure() throws Exception {
        // Given
        when(chatService.translateMessage(1, "en")).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/chat/translate/1")
                        .header("Accept-Language", "en")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Message or translation failed"));
    }

    @Test
    @DisplayName("POST /api/chat/room/{roomId}/leave - 채팅방 나가기")
    @WithMockUser(username = "testUser")
    void leaveChatRoom() throws Exception {
        // Given
        doNothing().when(chatService).markMessagesAsRead(1, "testUser");

        // When & Then
        mockMvc.perform(post("/api/chat/room/1/leave")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully left the chat room."));
    }

    @Test
    @DisplayName("POST /api/chat/room/{roomId}/leave - 채팅방 나가기 실패")
    @WithMockUser(username = "testUser")
    void leaveChatRoomFailure() throws Exception {
        // Given
        doThrow(new RuntimeException("Error")).when(chatService).markMessagesAsRead(1, "testUser");

        // When & Then
        mockMvc.perform(post("/api/chat/room/1/leave")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to leave chat room."));
    }
}
