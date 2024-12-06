package uni.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uni.backend.domain.ChatMessage;
import uni.backend.domain.ChatRoom;
import uni.backend.domain.User;
import uni.backend.domain.dto.ChatMessageRequest;
import uni.backend.domain.dto.ChatRoomRequest;
import uni.backend.repository.ChatMessageRepository;
import uni.backend.repository.ChatRoomRepository;
import uni.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    private ChatService chatService;
    private ChatRoomRepository chatRoomRepository;
    private ChatMessageRepository chatMessageRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        chatRoomRepository = mock(ChatRoomRepository.class);
        chatMessageRepository = mock(ChatMessageRepository.class);
        userRepository = mock(UserRepository.class);
        var translationService = mock(TranslationService.class);
        var userStatusScheduler = mock(UserStatusScheduler.class);

        chatService = new ChatService(
                chatRoomRepository,
                chatMessageRepository,
                userRepository,
                translationService,
                userStatusScheduler
        );
    }

    @Test
    void testCreateChatRoom() {
        // given
        var senderEmail = "sender@example.com";
        var request = ChatRoomRequest.builder().receiverId(2).build();
        var sender = User.builder().userId(1).email(senderEmail).name("Sender").build();
        var receiver = User.builder().userId(2).email("receiver@example.com").name("Receiver").build();

        // Mock 설정
        when(userRepository.findByEmail(senderEmail)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2)).thenReturn(Optional.of(receiver));
        when(chatRoomRepository.findBySenderAndReceiver(sender, receiver)).thenReturn(Optional.empty());

        var chatRoom = ChatRoom.builder()
                .chatRoomId(1)
                .sender(sender)
                .receiver(receiver)
                .build();
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

        // 추가 Mock 설정: findById
        when(chatRoomRepository.findById(1)).thenReturn(Optional.of(chatRoom));

        // when
        var response = chatService.createChatRoom(senderEmail, request);

        // then
        assertNotNull(response);
        assertEquals(1, response.getChatRoomId());

        // Verify Mock 동작
        verify(userRepository).findByEmail(senderEmail);
        verify(userRepository).findById(2);
        verify(chatRoomRepository).findBySenderAndReceiver(sender, receiver);
        verify(chatRoomRepository).save(any(ChatRoom.class));
        verify(chatRoomRepository).findById(1);
    }

    @Test
    void testSendMessage() {
        // given
        var request = ChatMessageRequest.builder().roomId(1).content("Hello").build();
        var senderEmail = "sender@example.com";
        var sender = User.builder().userId(1).email(senderEmail).name("Sender").build();
        var receiver = User.builder().userId(2).email("receiver@example.com").name("Receiver").build();
        var chatRoom = ChatRoom.builder().chatRoomId(1).sender(sender).receiver(receiver).build();

        when(userRepository.findByEmail(senderEmail)).thenReturn(Optional.of(sender));
        when(chatRoomRepository.findById(1)).thenReturn(Optional.of(chatRoom));

        var message = ChatMessage.builder()
                .messageId(1)
                .chatRoom(chatRoom)
                .sender(sender)
                .receiver(receiver)
                .content("Hello")
                .sendAt(LocalDateTime.now())
                .isRead(false)
                .build();

        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(message);

        // when
        var response = chatService.sendMessage(request, senderEmail, 1);

        // then
        assertNotNull(response);
        assertEquals("Hello", response.getContent());
        verify(chatMessageRepository).save(any(ChatMessage.class));
        verify(chatRoomRepository).save(chatRoom);
    }

    @Test
    void testMarkMessagesAsRead() {
        // given
        var roomId = 1;
        var username = "receiver@example.com";
        var receiver = User.builder().userId(2).email(username).name("Receiver").build();
        var chatRoom = ChatRoom.builder()
                .chatRoomId(1)
                .sender(User.builder().userId(1).email("sender@example.com").name("Sender").build())
                .receiver(receiver)
                .build();

        var message = ChatMessage.builder()
                .messageId(1)
                .chatRoom(chatRoom)
                .sender(chatRoom.getSender())
                .receiver(receiver)
                .content("Hi")
                .sendAt(LocalDateTime.now())
                .isRead(false)
                .build();

        chatRoom.setChatMessages(List.of(message));

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(receiver));

        // when
        chatService.markMessagesAsRead(roomId, username);

        // then
        assertTrue(message.isRead());
        verify(chatMessageRepository).saveAll(chatRoom.getChatMessages());
    }

    @Test
    void testGetChatMessages() {
        // given
        var roomId = 1;
        var chatRoom = ChatRoom.builder()
                .chatRoomId(1)
                .sender(User.builder().userId(1).email("sender@example.com").name("Sender").build())
                .receiver(User.builder().userId(2).email("receiver@example.com").name("Receiver").build())
                .build();

        var message = ChatMessage.builder()
                .messageId(1)
                .chatRoom(chatRoom)
                .sender(chatRoom.getSender())
                .receiver(chatRoom.getReceiver())
                .content("Hi")
                .sendAt(LocalDateTime.now())
                .isRead(false)
                .build();

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(chatMessageRepository.findByChatRoom(chatRoom)).thenReturn(List.of(message));

        // when
        var messages = chatService.getChatMessages(roomId);

        // then
        assertNotNull(messages);
        assertEquals(1, messages.size());
        assertEquals("Hi", messages.getFirst().getContent());
    }
}