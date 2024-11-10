package uni.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.ChatMessage;
import uni.backend.domain.ChatRoom;
import uni.backend.domain.User;
import uni.backend.domain.dto.ChatMessageRequest;
import uni.backend.domain.dto.ChatRoomRequest;
import uni.backend.repository.ChatMessageRepository;
import uni.backend.repository.ChatRoomRepository;
import uni.backend.repository.UserRepository;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

class ChatServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatService chatService;

    @Mock
    private Principal principal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 채팅방_생성_성공() {
        // Given
        String senderEmail = "sender@example.com";
        Integer receiverId = 2;
        ChatRoomRequest request = new ChatRoomRequest();
        request.setReceiverId(receiverId);
        request.setCreatedAt(LocalDateTime.now());

        User sender = new User();
        sender.setUserId(1);
        sender.setEmail(senderEmail);

        User receiver = new User();
        receiver.setUserId(receiverId);

        when(userRepository.findByEmail(senderEmail)).thenReturn(sender);
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(chatRoomRepository.findBySenderAndReceiver(sender, receiver)).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ChatRoom chatRoom = chatService.createChatRoom(senderEmail, request);

        // Then
        assertNotNull(chatRoom);

        ArgumentCaptor<ChatRoom> chatRoomCaptor = ArgumentCaptor.forClass(ChatRoom.class);
        verify(chatRoomRepository).save(chatRoomCaptor.capture());
        ChatRoom capturedChatRoom = chatRoomCaptor.getValue();

        assertEquals(sender, capturedChatRoom.getSender());
        assertEquals(receiver, capturedChatRoom.getReceiver());
        assertEquals(request.getCreatedAt(), capturedChatRoom.getCreatedAt());
    }

    @Test
    void 채팅방_생성_ReceiverNotFound() {
        // Given
        String senderEmail = "sender@email.com";
        Integer receiverId = 2;
        ChatRoomRequest request = new ChatRoomRequest();
        request.setReceiverId(receiverId);

        User sender = new User();
        sender.setEmail(senderEmail);

        when(userRepository.findByEmail(senderEmail)).thenReturn(sender);
        when(userRepository.findById(receiverId)).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            chatService.createChatRoom(senderEmail, request);
        });

        // Then
        assertEquals("Receiver not found", exception.getMessage());
    }

    @Test
    void 로그인_사용자_ID찾기_성공() {
        // Given
        String email = "principal@email.com";
        when(principal.getName()).thenReturn(email);

        User user = new User();
        user.setUserId(1);
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(user);

        // When
        Integer senderId = chatService.getSenderIdFromPrincipal(principal);

        // Then
        assertEquals(1, senderId);
    }

    @Test
    void 로그인_사용자_ID찾기_UserNotFound() {
        // Given
        String email = "principal@email.com";
        when(principal.getName()).thenReturn(email);

        when(userRepository.findByEmail(email)).thenReturn(null);

        // When
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            chatService.getSenderIdFromPrincipal(principal);
        });

        // Then
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @Transactional
    void 메시지_저장_성공() {
        // Given
        Integer roomId = 1;
        Integer senderId = 1;
        Integer receiverId = 2;
        String content = "Hello, world!";

        ChatMessageRequest messageRequest = new ChatMessageRequest();
        messageRequest.setRoomId(roomId);
        messageRequest.setSenderId(senderId);
        messageRequest.setReceiverId(receiverId);
        messageRequest.setContent(content);

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatRoomId(roomId);

        User sender = new User();
        sender.setUserId(senderId);

        User receiver = new User();
        receiver.setUserId(receiverId);

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ChatMessage chatMessage = chatService.sendMessage(messageRequest);

        // Then
        assertNotNull(chatMessage);

        ArgumentCaptor<ChatMessage> chatMessageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository).save(chatMessageCaptor.capture());
        ChatMessage capturedChatMessage = chatMessageCaptor.getValue();

        assertEquals(content, capturedChatMessage.getContent());
        assertEquals(sender, capturedChatMessage.getSender());
        assertEquals(receiver, capturedChatMessage.getReceiver());
        assertEquals(chatRoom, capturedChatMessage.getChatRoom());
    }


    @Test
    void 메시지_저장_ChatRoomNotFound() {
        // Given
        Integer roomId = 1;
        ChatMessageRequest messageRequest = new ChatMessageRequest();
        messageRequest.setRoomId(roomId);

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            chatService.sendMessage(messageRequest);
        });

        // Then
        assertEquals("Chat room not found", exception.getMessage());
    }

    @Test
    void 메시지_저장_SenderNotFound() {
        // Given
        Integer roomId = 1;
        Integer senderId = 1;
        ChatMessageRequest messageRequest = new ChatMessageRequest();
        messageRequest.setRoomId(roomId);
        messageRequest.setSenderId(senderId);

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatRoomId(roomId);

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findById(senderId)).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            chatService.sendMessage(messageRequest);
        });

        // Then
        assertEquals("Sender not found", exception.getMessage());
    }

    @Test
    void 메시지_저장_ReceiverNotFound() {
        // Given
        Integer roomId = 1;
        Integer senderId = 1;
        Integer receiverId = 2;
        ChatMessageRequest messageRequest = new ChatMessageRequest();
        messageRequest.setRoomId(roomId);
        messageRequest.setSenderId(senderId);
        messageRequest.setReceiverId(receiverId);

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatRoomId(roomId);

        User sender = new User();
        sender.setUserId(senderId);

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            chatService.sendMessage(messageRequest);
        });

        // Then
        assertEquals("Receiver not found", exception.getMessage());
    }
}
