package uni.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.ChatMessage;
import uni.backend.domain.ChatRoom;
import uni.backend.domain.Role;
import uni.backend.domain.User;
import uni.backend.domain.dto.ChatMessageRequest;
import uni.backend.repository.ChatMessageRepository;
import uni.backend.repository.ChatRoomRepository;
import uni.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    public void testSendMessage() {
        // given
        User user1 = User.builder()
                .email("user1@example.com")
                .password("password")
                .name("User One")
                .role(Role.KOREAN)
                .status("ACTIVE")
                .lastVerification(LocalDateTime.now())
                .build();
        userRepository.save(user1);

        User user2 = User.builder()
                .email("user2@example.com")
                .password("password")
                .name("User Two")
                .role(Role.KOREAN)
                .status("ACTIVE")
                .lastVerification(LocalDateTime.now())
                .build();
        userRepository.save(user2);

        ChatRoom chatRoom = ChatRoom.builder()
                .user1(user1)
                .user2(user2)
                .createdAt(LocalDateTime.now())
                .build();
        chatRoomRepository.save(chatRoom);

        ChatMessageRequest messageRequest = new ChatMessageRequest();
        messageRequest.setContent("Hello, User Two!");

        // when
        chatService.sendMessage(chatRoom.getChatRoomId(), user1.getUserId(), messageRequest);

        // then
        List<ChatMessage> messages = chatMessageRepository.findAll();
        assertFalse(messages.isEmpty());
        ChatMessage savedMessage = messages.get(0);
        assertEquals("Hello, User Two!", savedMessage.getContent());
        assertEquals(user1.getUserId(), savedMessage.getSender().getUserId());
        assertEquals(chatRoom.getChatRoomId(), savedMessage.getChatRoom().getChatRoomId());
    }

    @Test
    @Transactional
    public void testSendMessage2() {
        Integer roomId = 1;
        Integer senderId = 1;
        ChatMessageRequest messageRequest = new ChatMessageRequest();
        messageRequest.setContent("테스트 메시지");

        chatService.sendMessage(roomId, senderId, messageRequest);

        // 메시지가 저장되었는지 확인
        List<ChatMessage> messages = chatMessageRepository.findAll();
        assertFalse(messages.isEmpty());
    }
}
