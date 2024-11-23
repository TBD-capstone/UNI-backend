package uni.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uni.backend.domain.ChatMessage;
import uni.backend.domain.ChatRoom;
import uni.backend.domain.User;
import uni.backend.domain.dto.ChatMessageRequest;
import uni.backend.domain.dto.ChatMessageResponse;
import uni.backend.domain.dto.ChatRoomRequest;
import uni.backend.domain.dto.ChatRoomResponse;
import uni.backend.repository.ChatMessageRepository;
import uni.backend.repository.ChatRoomRepository;
import uni.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatService chatService;

    private User sender;
    private User receiver;
    private ChatRoom chatRoom;
    private ChatMessageRequest chatMessageRequest;
    private ChatRoomRequest chatRoomRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Given
        sender = User.builder().userId(1).email("sender@example.com").build();
        receiver = User.builder().userId(2).email("receiver@example.com").build();

        chatRoom = ChatRoom.builder()
                .chatRoomId(123)
                .sender(sender)
                .receiver(receiver)
                .build();

        chatMessageRequest = ChatMessageRequest.builder()
                .roomId(chatRoom.getChatRoomId())
                .content("Hello, World!")
                .build();

        chatRoomRequest = ChatRoomRequest.builder()
                .receiverId(receiver.getUserId())
                .build();

        when(chatRoomRepository.findById(chatRoom.getChatRoomId())).thenReturn(Optional.of(chatRoom));
        when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiver.getUserId())).thenReturn(Optional.of(receiver));
        when(chatRoomRepository.findBySenderAndReceiver(sender, receiver)).thenReturn(Optional.of(chatRoom));
    }


    @Test
    void testCreateChatRoom() {
        // Given
        when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiver.getUserId())).thenReturn(Optional.of(receiver));
        when(chatRoomRepository.findBySenderAndReceiver(sender, receiver)).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

        // When
        ChatRoomResponse response = chatService.createChatRoom(sender.getEmail(), chatRoomRequest);

        // Then
        verify(userRepository, times(1)).findByEmail(sender.getEmail());
        verify(userRepository, times(1)).findById(receiver.getUserId());
        verify(chatRoomRepository, times(1)).findBySenderAndReceiver(sender, receiver);
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class)); // save() 호출 검증

        assertNotNull(response);
        assertEquals(chatRoom.getChatRoomId(), response.getChatRoomId());
        assertEquals(sender.getUserId(), response.getMyId());
        assertEquals(receiver.getUserId(), response.getOtherId());
    }

    @Test
    void testSendMessage() {
        // Given
        when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
        when(chatRoomRepository.findById(chatRoom.getChatRoomId())).thenReturn(Optional.of(chatRoom));
        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ChatMessageResponse response = chatService.sendMessage(chatMessageRequest, sender.getEmail(), null);

        // Then
        verify(userRepository, times(1)).findByEmail(sender.getEmail());
        verify(chatRoomRepository, times(1)).findById(chatMessageRequest.getRoomId());
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));

        assertNotNull(response);
        assertEquals(chatRoom.getChatRoomId(), response.getRoomId());
        assertEquals(sender.getUserId(), response.getSenderId());
        assertEquals(receiver.getUserId(), response.getReceiverId());
        assertEquals(chatMessageRequest.getContent(), response.getContent());
    }

    @Test
    void testGetChatRoomsForUser() {
        // Given
        when(chatRoomRepository.findBySenderOrReceiver(sender, sender)).thenReturn(List.of(chatRoom));

        // When
        List<ChatRoomResponse> responses = chatService.getChatRoomsForUser(sender.getEmail());

        // Then
        verify(userRepository, times(1)).findByEmail(sender.getEmail());
        verify(chatRoomRepository, times(1)).findBySenderOrReceiver(sender, sender);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(chatRoom.getChatRoomId(), responses.get(0).getChatRoomId());
    }

    @Test
    void testGetChatMessages() {
        // Given
        ChatMessage chatMessage = ChatMessage.builder()
                .messageId(1)
                .chatRoom(chatRoom)
                .sender(sender)
                .receiver(receiver)
                .content("Hello")
                .sendAt(LocalDateTime.now())
                .build();
        when(chatRoomRepository.findById(chatRoom.getChatRoomId())).thenReturn(Optional.of(chatRoom));
        when(chatMessageRepository.findByChatRoom(chatRoom)).thenReturn(Arrays.asList(chatMessage));

        // When
        List<ChatMessageResponse> responses = chatService.getChatMessages(chatRoom.getChatRoomId());

        // Then
        verify(chatRoomRepository, times(1)).findById(chatRoom.getChatRoomId());
        verify(chatMessageRepository, times(1)).findByChatRoom(chatRoom);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(chatMessage.getContent(), responses.get(0).getContent());
    }

    @Test
    void testFindUserByEmailNotFound() {
        // Given
        when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.empty());

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> chatService.getChatRoomsForUser(sender.getEmail()));

        // Then
        assertEquals("User not found with email: " + sender.getEmail(), exception.getMessage());
    }

    @Test
    void testFindChatRoomByIdNotFound() {
        // Given
        when(chatRoomRepository.findById(chatRoom.getChatRoomId())).thenReturn(Optional.empty());

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> chatService.getChatMessages(chatRoom.getChatRoomId()));

        // Then
        assertEquals("Chat room not found with ID: " + chatRoom.getChatRoomId(), exception.getMessage());
    }
}
