package uni.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import uni.backend.domain.dto.*;
import uni.backend.service.ChatService;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @InjectMocks
    private ChatController chatController;

    @Mock
    private Principal principal;

    @BeforeEach
    void setUp() {
        lenient().when(principal.getName()).thenReturn("user@example.com");
    }

    @Test
    void testGetChatRooms() {
        // Given
        List<ChatRoomResponse> chatRooms = List.of(
                ChatRoomResponse.builder().chatRoomId(1).build(),
                ChatRoomResponse.builder().chatRoomId(2).build()
        );
        when(chatService.getChatRoomsForUser(principal.getName())).thenReturn(chatRooms);

        // When
        ResponseEntity<List<ChatRoomResponse>> response = chatController.getChatRooms(principal);

        // Then
        verify(chatService, times(1)).getChatRoomsForUser(principal.getName());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testRequestChat() {
        // Given
        ChatRoomRequest request = ChatRoomRequest.builder().receiverId(2).build();
        ChatRoomResponse responseMock = ChatRoomResponse.builder().chatRoomId(1).build();
        when(chatService.createChatRoom(principal.getName(), request)).thenReturn(responseMock);

        // When
        ResponseEntity<ChatRoomResponse> response = chatController.requestChat(request, principal);

        // Then
        verify(chatService, times(1)).createChatRoom(principal.getName(), request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getChatRoomId());
    }

    @Test
    void testGetChatRoomMessages() {
        // Given
        List<ChatMessageResponse> messages = List.of(
                ChatMessageResponse.builder().messageId(1).content("Hello").build(),
                ChatMessageResponse.builder().messageId(2).content("World").build()
        );
        when(chatService.getChatMessages(1)).thenReturn(messages);

        // When
        ResponseEntity<List<ChatMessageResponse>> response = chatController.getChatRoomMessages(1);

        // Then
        verify(chatService, times(1)).getChatMessages(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testSendWebSocketMessage() {
        // Given
        ChatMessageRequest messageRequest = ChatMessageRequest.builder().roomId(1).content("Hello").build();
        ChatMessageResponse responseMock = ChatMessageResponse.builder().messageId(1).content("Hello").build();
        when(chatService.sendMessage(messageRequest, principal.getName(), null)).thenReturn(responseMock);

        // When
        chatController.sendWebSocketMessage(messageRequest, principal);

        // Then
        verify(chatService, times(1)).sendMessage(messageRequest, principal.getName(), null);
        verify(messagingTemplate, times(1)).convertAndSend("/sub/chat/room/1", responseMock);
        verify(messagingTemplate, times(1)).convertAndSend("/sub/user/" + responseMock.getReceiverId(), responseMock);
    }

    @Test
    void testSendRestMessage() {
        // Given
        ChatMessageRequest messageRequest = ChatMessageRequest.builder().roomId(1).content("Hello").build();
        ChatMessageResponse responseMock = ChatMessageResponse.builder().messageId(1).content("Hello").build();
        when(chatService.sendMessage(messageRequest, principal.getName(), 1)).thenReturn(responseMock);

        // When
        ResponseEntity<ChatMessageResponse> response = chatController.sendRestMessage(1, messageRequest, principal);

        // Then
        verify(chatService, times(1)).sendMessage(messageRequest, principal.getName(), 1);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getMessageId());
    }

    @Test
    void testTranslateChatMessage() {
        // Given
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader("Accept-Language")).thenReturn("en");
        when(chatService.translateMessage(1, "en")).thenReturn("Translated Text");

        // When
        ResponseEntity<String> response = chatController.translateChatMessage(1, mockRequest);

        // Then
        verify(chatService, times(1)).translateMessage(1, "en");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Translated Text", response.getBody());
    }

    @Test
    void testTranslateChatMessageNotFound() {
        // Given
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader("Accept-Language")).thenReturn("en");
        when(chatService.translateMessage(1, "en")).thenReturn(null);

        // When
        ResponseEntity<String> response = chatController.translateChatMessage(1, mockRequest);

        // Then
        verify(chatService, times(1)).translateMessage(1, "en");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Message or translation failed", response.getBody());
    }
}