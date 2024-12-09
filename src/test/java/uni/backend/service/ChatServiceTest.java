package uni.backend.service;

import java.security.Principal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import uni.backend.controller.ChatController;
import uni.backend.domain.ChatMessage;
import uni.backend.domain.ChatRoom;
import uni.backend.domain.User;
import uni.backend.domain.dto.*;
import uni.backend.repository.ChatMessageRepository;
import uni.backend.repository.ChatRoomRepository;
import uni.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    private ChatRoomRepository chatRoomRepository;
    private ChatMessageRepository chatMessageRepository;
    private UserRepository userRepository;
    private TranslationService translationService;
    private ChatController chatController;

    @Mock
    private ChatService chatService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatController = new ChatController(chatService, messagingTemplate); // 초기화 추가
        chatRoomRepository = mock(ChatRoomRepository.class);
        chatMessageRepository = mock(ChatMessageRepository.class);
        userRepository = mock(UserRepository.class);
        translationService = mock(TranslationService.class); // 필드에 직접 할당
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
        var receiver = User.builder().userId(2).email("receiver@example.com").name("Receiver")
            .build();

        // Mock 설정
        when(userRepository.findByEmail(senderEmail)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2)).thenReturn(Optional.of(receiver));
        when(chatRoomRepository.findBySenderAndReceiver(sender, receiver)).thenReturn(
            Optional.empty());

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
    void testSendMessageWithEmptyContent() {
        // given
        var request = ChatMessageRequest.builder().roomId(1).content("").build(); // 빈 content
        var senderEmail = "sender@example.com";

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            chatService.sendMessage(request, senderEmail, 1);
        });

        assertEquals("Message content cannot be null or empty", exception.getMessage());
    }

    @Test
    void testSendMessageWithDirectRoomId() {
        // given
        var request = ChatMessageRequest.builder().roomId(null).content("Hello")
            .build(); // RoomId는 null
        var senderEmail = "sender@example.com";
        Integer roomId = 5; // 명시적으로 roomId 설정
        var sender = User.builder().userId(1).email(senderEmail).build();
        var receiver = User.builder().userId(2).email("receiver@example.com").build();
        var chatRoom = ChatRoom.builder()
            .chatRoomId(roomId)
            .sender(sender) // sender 설정
            .receiver(receiver) // receiver 설정
            .build();

        var message = ChatMessage.builder()
            .messageId(1)
            .chatRoom(chatRoom)
            .sender(sender)
            .receiver(receiver)
            .content("Hello")
            .sendAt(LocalDateTime.now())
            .isRead(false)
            .build();

        // Mock 설정
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findByEmail(senderEmail)).thenReturn(Optional.of(sender));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(message); // Mock 반환값 설정

        // when
        var response = chatService.sendMessage(request, senderEmail, roomId);

        // then
        assertNotNull(response); // 반환된 응답이 null이 아님을 확인
        assertEquals("Hello", response.getContent()); // 반환된 메시지 내용 확인
        verify(chatRoomRepository).findById(roomId); // roomId가 명시적으로 사용됨 확인
    }

    @Test
    void testSendMessageWithRequestRoomId() {
        // given
        Integer requestRoomId = 10; // request에 포함된 RoomId
        var request = ChatMessageRequest.builder().roomId(requestRoomId).content("Hello")
            .build(); // RoomId는 요청에서 가져옴
        var senderEmail = "sender@example.com";
        Integer roomId = null; // 명시적으로 roomId가 null
        var sender = User.builder().userId(1).email(senderEmail).name("Sender").build();
        var receiver = User.builder().userId(2).email("receiver@example.com").name("Receiver")
            .build();
        var chatRoom = ChatRoom.builder()
            .chatRoomId(requestRoomId)
            .sender(sender) // sender 설정
            .receiver(receiver) // receiver 설정
            .build();

        var message = ChatMessage.builder()
            .messageId(1)
            .chatRoom(chatRoom)
            .sender(sender)
            .receiver(receiver)
            .content("Hello")
            .sendAt(LocalDateTime.now())
            .isRead(false)
            .build();

        // Mock 설정
        when(chatRoomRepository.findById(requestRoomId)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findByEmail(senderEmail)).thenReturn(Optional.of(sender));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(message); // Mock 반환값 설정

        // when
        var response = chatService.sendMessage(request, senderEmail, roomId);

        // then
        assertNotNull(response); // 반환된 응답이 null이 아님을 확인
        assertEquals("Hello", response.getContent()); // 반환된 메시지 내용 확인
        verify(chatRoomRepository).findById(requestRoomId); // request.getRoomId()가 사용됨 확인
    }

    @Test
    void testSendMessageWithNullContent() {
        // given
        var request = ChatMessageRequest.builder().roomId(1).content(null).build(); // null content
        var senderEmail = "sender@example.com";

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            chatService.sendMessage(request, senderEmail, 1);
        });

        assertEquals("Message content cannot be null or empty", exception.getMessage());
    }

    @Test
    void testSendMessageSuccessfullySenderIsChatRoomSender() {
        // given
        var request = ChatMessageRequest.builder().roomId(1).content("Hello")
            .build(); // 유효한 content
        var senderEmail = "sender@example.com";
        var sender = User.builder().userId(1).email(senderEmail).name("Sender").build();
        var receiver = User.builder().userId(2).email("receiver@example.com").name("Receiver")
            .build();
        var chatRoom = ChatRoom.builder().chatRoomId(1).sender(sender).receiver(receiver)
            .build(); // sender가 chatRoom의 sender

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

        // Verify chatRoom update for sender as chatRoom sender
        assertEquals(LocalDateTime.now().getMinute(),
            chatRoom.getSenderLastMessageAt().getMinute()); // senderLastMessageAt 갱신 확인
        assertEquals(1, chatRoom.getReceiverUnreadCount()); // receiverUnreadCount가 1 증가했는지 확인

        verify(chatMessageRepository).save(any(ChatMessage.class));
        verify(chatRoomRepository).save(chatRoom); // chatRoom이 업데이트 되었는지 확인
    }

    @Test
    void testSendMessageSuccessfullySenderIsChatRoomReceiver() {
        // given
        var request = ChatMessageRequest.builder().roomId(1).content("Hello")
            .build(); // 유효한 content
        var senderEmail = "sender@example.com";
        var sender = User.builder().userId(1).email(senderEmail).name("Sender").build();
        var receiver = User.builder().userId(2).email("receiver@example.com").name("Receiver")
            .build();
        var chatRoom = ChatRoom.builder().chatRoomId(1).sender(receiver).receiver(sender)
            .build(); // sender가 chatRoom의 receiver

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

        // Verify chatRoom update for sender as chatRoom receiver
        assertEquals(LocalDateTime.now().getMinute(),
            chatRoom.getReceiverLastMessageAt().getMinute()); // receiverLastMessageAt 갱신 확인
        assertEquals(1, chatRoom.getSenderUnreadCount()); // senderUnreadCount가 1 증가했는지 확인

        verify(chatMessageRepository).save(any(ChatMessage.class));
        verify(chatRoomRepository).save(chatRoom); // chatRoom이 업데이트 되었는지 확인
    }

    @Test
    void testMarkMessagesAsReadForSender() {
        // given
        var roomId = 1;
        var username = "receiver@example.com"; // sender 가 읽은 경우
        var sender = User.builder().userId(1).email("sender@example.com").name("Sender").build();
        var receiver = User.builder().userId(2).email(username).name("Receiver").build();
        var chatRoom = ChatRoom.builder()
            .chatRoomId(1)
            .sender(sender)
            .receiver(receiver)
            .build();

        var message = ChatMessage.builder()
            .messageId(1)
            .chatRoom(chatRoom)
            .sender(receiver)
            .receiver(sender)
            .content("Hi")
            .sendAt(LocalDateTime.now())
            .isRead(false)
            .build();

        chatRoom.setChatMessages(List.of(message));

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(sender));

        // when
        chatService.markMessagesAsRead(roomId, username);

        // then
        assertTrue(message.isRead(), "The message should be marked as read");
        assertEquals(0, chatRoom.getReceiverUnreadCount(),
            "The receiver's unread count should be 0 after reading the message");
        verify(chatMessageRepository).saveAll(chatRoom.getChatMessages());
    }

    @Test
    void testMarkMessagesAsReadForReceiver() {
        // given
        var roomId = 1;
        var username = "receiver@example.com"; // receiver가 읽은 경우
        var sender = User.builder().userId(1).email("sender@example.com").name("Sender").build();
        var receiver = User.builder().userId(2).email(username).name("Receiver").build();
        var chatRoom = ChatRoom.builder()
            .chatRoomId(1)
            .sender(sender)
            .receiver(receiver)
            .build();

        var message = ChatMessage.builder()
            .messageId(1)
            .chatRoom(chatRoom)
            .sender(sender)
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
        assertTrue(message.isRead(), "The message should be marked as read");
        assertEquals(0, chatRoom.getReceiverUnreadCount(),
            "The receiver's unread count should be 0 after reading the message");
        verify(chatMessageRepository).saveAll(chatRoom.getChatMessages());
    }

    @Test
    void testMarkMessagesAsRead_CaseNotReadAndWrongReceiver() {
        // given
        var roomId = 1;
        var username = "receiver@example.com";
        var sender = User.builder().userId(1).email("sender@example.com").build();
        var receiver = User.builder().userId(2).email(username).build();
        var otherReceiver = User.builder().userId(3).email("other@example.com").build();
        var chatRoom = ChatRoom.builder().chatRoomId(roomId).sender(sender).receiver(receiver)
            .build();

        var message = ChatMessage.builder()
            .chatRoom(chatRoom)
            .receiver(otherReceiver) // 잘못된 receiver
            .isRead(false) // 아직 읽지 않은 메시지
            .build();

        chatRoom.setChatMessages(List.of(message));
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(receiver));

        // when
        chatService.markMessagesAsRead(roomId, username);

        // then
        assertFalse(message.isRead(),
            "The message should remain unread as it is for a different receiver");
        verify(chatMessageRepository, never()).saveAll(any()); // 저장 동작이 발생하지 않아야 함
        verify(chatRoomRepository).save(chatRoom); // 채팅방은 저장될 수 있음
    }

    @Test
    void testMarkMessagesAsRead_CaseAlreadyReadAndCorrectReceiver() {
        // given
        var roomId = 1;
        var username = "receiver@example.com";
        var sender = User.builder().userId(1).email("sender@example.com").build();
        var receiver = User.builder().userId(2).email(username).build();
        var chatRoom = ChatRoom.builder().chatRoomId(roomId).sender(sender).receiver(receiver)
            .build();

        var message = ChatMessage.builder()
            .chatRoom(chatRoom)
            .receiver(receiver) // 올바른 receiver
            .isRead(true) // 이미 읽은 메시지
            .build();

        chatRoom.setChatMessages(List.of(message));
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(receiver));

        // when
        chatService.markMessagesAsRead(roomId, username);

        // then
        assertTrue(message.isRead(), "The message should remain read");
        verify(chatMessageRepository, never()).saveAll(any()); // 저장 동작이 발생하지 않아야 함
    }

    @Test
    void testTranslateMessage() {
        // given
        var messageId = 1;
        var acceptLanguage = "en";
        var originalMessage = "Hi";
        var targetLanguage = "en";

        var translationResponse = new TranslationResponse();
        IndividualTranslationResponse translation = new IndividualTranslationResponse();
        translation.setText("Hello");
        translationResponse.setTranslations(List.of(translation));

        // Mock the repository and services
        when(chatMessageRepository.findById(messageId))
            .thenReturn(Optional.of(ChatMessage.builder()
                .messageId(messageId)
                .content(originalMessage)
                .build()));
        when(translationService.determineTargetLanguage(acceptLanguage)).thenReturn(targetLanguage);
        when(translationService.translate(any(TranslationRequest.class))).thenReturn(
            translationResponse);

        // when
        String translatedMessage = chatService.translateMessage(messageId, acceptLanguage);

        // then
        assertNotNull(translatedMessage);
        assertEquals("Hello", translatedMessage); // The translated message should be "Hello"
    }

    @Test
    void testTranslateMessageWhenMessageNotFound() {
        // given
        Integer messageId = 1;
        String acceptLanguage = "en"; // 영어로 번역 요청

        // 메시지가 없는 경우를 Mock 설정
        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            chatService.translateMessage(messageId, acceptLanguage);
        });

        assertEquals("Message with ID 1 not found", exception.getMessage());
    }

    @Test
    void testTranslateMessageWhenTranslationFails() {
        // given
        Integer messageId = 1;
        String acceptLanguage = "en";  // 영어로 번역 요청
        String originalMessage = "안녕하세요";  // 원본 메시지

        // Mock the translation service
        var translationService = mock(TranslationService.class);
        var chatRoomRepository = mock(ChatRoomRepository.class);
        var chatMessageRepository = mock(ChatMessageRepository.class);
        var userRepository = mock(UserRepository.class);
        var userStatusScheduler = mock(UserStatusScheduler.class);
        chatService = new ChatService(chatRoomRepository, chatMessageRepository, userRepository,
            translationService, userStatusScheduler);

        // Mock the translation response (empty response)
        TranslationResponse translationResponse = mock(TranslationResponse.class);
        when(translationService.translate(any(TranslationRequest.class))).thenReturn(
            translationResponse);
        when(translationResponse.getTranslations()).thenReturn(List.of());

        // Mock the method to get the message by id
        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(ChatMessage.builder()
            .messageId(messageId)
            .content(originalMessage)
            .build()));

        // when
        String result = chatService.translateMessage(messageId, acceptLanguage);

        // then
        assertNull(result);  // 번역이 실패했으므로 null을 반환해야 함
    }

    @Test
    void testTranslateMessageWhenOriginalMessageIsNull() {
        // given
        Integer messageId = 1;
        String acceptLanguage = "en";

        // ChatService를 Spy로 생성
        ChatService spyChatService = spy(chatService);

        // Mock 설정: getMessageById가 null을 반환하도록 설정
        doReturn(null).when(spyChatService).getMessageById(messageId);

        // when
        String result = spyChatService.translateMessage(messageId, acceptLanguage);

        // then
        assertNull(result); // 메시지가 null이므로 null 반환
    }

    @Test
    void testGetChatRoomsForUser() {
        // given
        var email = "user@example.com";
        var user = User.builder().userId(1).email(email).name("User").build();

        var chatRoom1 = ChatRoom.builder()
            .chatRoomId(1)
            .sender(user)
            .receiver(
                User.builder().userId(2).email("receiver1@example.com").name("Receiver1").build())
            .build();

        var chatRoom2 = ChatRoom.builder()
            .chatRoomId(2)
            .sender(user)
            .receiver(
                User.builder().userId(3).email("receiver2@example.com").name("Receiver2").build())
            .build();

        // Mock 설정
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(chatRoomRepository.findBySenderOrReceiver(user, user))
            .thenReturn(List.of(chatRoom1, chatRoom2));

        // 추가 Mock 설정: findById
        when(chatRoomRepository.findById(1)).thenReturn(Optional.of(chatRoom1));
        when(chatRoomRepository.findById(2)).thenReturn(Optional.of(chatRoom2));

        // when
        var chatRooms = chatService.getChatRoomsForUser(email);

        // then
        assertNotNull(chatRooms);
        assertEquals(2, chatRooms.size()); // 두 개의 채팅방이 반환되어야 함
        assertEquals(1, chatRooms.get(0).getChatRoomId()); // 첫 번째 채팅방 ID는 1이어야 함
        assertEquals(2, chatRooms.get(1).getChatRoomId()); // 두 번째 채팅방 ID는 2이어야 함

        // Verify Mock 동작
        verify(userRepository).findByEmail(email);
        verify(chatRoomRepository).findBySenderOrReceiver(user, user);
        verify(chatRoomRepository).findById(1); // chatRoom1에 대한 findById 호출 검증
        verify(chatRoomRepository).findById(2); // chatRoom2에 대한 findById 호출 검증
    }

    @Test
    void testGetChatRoomsForUser_WhenCurrentUserIsReceiver() {
        // given
        var currentUser = User.builder()
            .userId(2)
            .email("receiver@example.com")
            .name("Receiver")
            .build();

        var sender = User.builder()
            .userId(1)
            .email("sender@example.com")
            .name("Sender")
            .build();

        var chatRoom = ChatRoom.builder()
            .chatRoomId(1)
            .sender(sender)
            .receiver(currentUser) // currentUser를 receiver로 설정
            .receiverUnreadCount(5) // 초기 unreadCount 설정
            .build();

        var message1 = ChatMessage.builder()
            .messageId(1)
            .chatRoom(chatRoom)
            .sender(sender)
            .receiver(currentUser)
            .isRead(false)
            .content("Hello")
            .build();

        chatRoom.setChatMessages(List.of(message1));

        // Mock 설정
        when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(
            Optional.of(currentUser));
        when(chatRoomRepository.findBySenderOrReceiver(currentUser, currentUser)).thenReturn(
            List.of(chatRoom));
        when(chatRoomRepository.findById(1)).thenReturn(Optional.of(chatRoom));
        when(chatMessageRepository.findByChatRoom(chatRoom)).thenReturn(List.of(message1));

        // when
        var chatRooms = chatService.getChatRoomsForUser(currentUser.getEmail());

        // then
        assertNotNull(chatRooms);
        assertEquals(1, chatRooms.size());
        var response = chatRooms.get(0);
        assertEquals(1, response.getChatRoomId());
        assertEquals(1, response.getUnreadCount()); // UnreadCount 값 검증
        assertEquals(currentUser.getUserId(), response.getMyId());
        assertEquals(sender.getUserId(), response.getOtherId());

        verify(chatRoomRepository).findBySenderOrReceiver(currentUser, currentUser);
        verify(chatRoomRepository).findById(1);
        verify(chatMessageRepository).findByChatRoom(chatRoom);
    }

    @Test
    void testGetChatRoomsForUser_WithMessages() {
        // given
        var email = "user@example.com";
        var user = User.builder().userId(1).email(email).name("User").build();

        var receiver1 = User.builder().userId(2).email("receiver1@example.com").name("Receiver1")
            .build();
        var receiver2 = User.builder().userId(3).email("receiver2@example.com").name("Receiver2")
            .build();

        var chatRoom1 = ChatRoom.builder()
            .chatRoomId(1)
            .sender(user)
            .receiver(receiver1)
            .build();

        var chatRoom2 = ChatRoom.builder()
            .chatRoomId(2)
            .sender(user)
            .receiver(receiver2)
            .build();

        // 메시지 설정
        var message1 = ChatMessage.builder()
            .messageId(1)
            .chatRoom(chatRoom1)
            .sender(user)
            .receiver(receiver1)
            .content("Hello")
            .sendAt(LocalDateTime.now())
            .isRead(false)
            .build();

        var message2 = ChatMessage.builder()
            .messageId(2)
            .chatRoom(chatRoom2)
            .sender(user)
            .receiver(receiver2)
            .content("Hi")
            .sendAt(LocalDateTime.now())
            .isRead(false)
            .build();

        chatRoom1.setChatMessages(List.of(message1)); // chatRoom1 메시지 추가
        chatRoom2.setChatMessages(List.of(message2)); // chatRoom2 메시지 추가

        // Mock 설정
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(chatRoomRepository.findBySenderOrReceiver(user, user)).thenReturn(
            List.of(chatRoom1, chatRoom2));
        when(chatRoomRepository.findById(1)).thenReturn(Optional.of(chatRoom1));
        when(chatRoomRepository.findById(2)).thenReturn(Optional.of(chatRoom2));
        when(chatMessageRepository.findByChatRoom(chatRoom1)).thenReturn(List.of(message1));
        when(chatMessageRepository.findByChatRoom(chatRoom2)).thenReturn(List.of(message2));

        // when
        var chatRooms = chatService.getChatRoomsForUser(email);

        // then
        assertNotNull(chatRooms);
        assertEquals(2, chatRooms.size());
        assertEquals(1, chatRooms.get(0).getChatRoomId());
        assertEquals(2, chatRooms.get(1).getChatRoomId());
        assertEquals("Hello", chatRooms.get(0).getChatMessages().get(0).getContent());
        assertEquals("Hi", chatRooms.get(1).getChatMessages().get(0).getContent());

        // Verify Mock 호출
        verify(userRepository).findByEmail(email);
        verify(chatRoomRepository).findBySenderOrReceiver(user, user);
        verify(chatRoomRepository).findById(1);
        verify(chatRoomRepository).findById(2);
        verify(chatMessageRepository).findByChatRoom(chatRoom1);
        verify(chatMessageRepository).findByChatRoom(chatRoom2);
    }

    @Test
    void testGetChatMessages() {
        // given
        var roomId = 1;
        var chatRoom = ChatRoom.builder()
            .chatRoomId(1)
            .sender(User.builder().userId(1).email("sender@example.com").name("Sender").build())
            .receiver(
                User.builder().userId(2).email("receiver@example.com").name("Receiver").build())
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

    @Test
    void testNotifyUnreadMessages() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // Sender에게 알림을 보내야 하는 ChatRoom
        User sender = User.builder().userId(1).email("sender@example.com").build();
        User receiver = User.builder().userId(2).email("receiver@example.com").build();

        ChatRoom chatRoomWithSenderNotification = ChatRoom.builder()
            .chatRoomId(1)
            .sender(sender)
            .receiver(receiver)
            .senderUnreadCount(5) // Sender가 읽지 않은 메시지가 있음
            .receiverLastMessageAt(now.minusDays(2)) // 마지막 메시지가 1일 이상 지남
            .build();

        ChatRoom chatRoomWithoutNotification = ChatRoom.builder()
            .chatRoomId(2)
            .sender(sender)
            .receiver(receiver)
            .senderUnreadCount(0) // 읽지 않은 메시지가 없음
            .receiverLastMessageAt(now.minusHours(12)) // 마지막 메시지가 1일 지나지 않음
            .build();

        when(chatRoomRepository.findAll())
            .thenReturn(List.of(chatRoomWithSenderNotification, chatRoomWithoutNotification));

        // ChatService의 notifyUnreadMessages를 실행
        chatService.notifyUnreadMessages();

        // Mocked repository가 호출되었는지 확인
        verify(chatRoomRepository, times(1)).findAll();

        // 알림이 필요한 조건만 검증
        assertEquals(5, chatRoomWithSenderNotification.getSenderUnreadCount());
        assertTrue(
            chatRoomWithSenderNotification.getReceiverLastMessageAt().isBefore(now.minusDays(1)));
    }

    @Test
    void testNotifyUnreadMessages_WhenReceiverHasUnreadMessages() {
        // given
        LocalDateTime now = LocalDateTime.now();

        User sender = User.builder().userId(1).email("sender@example.com").build();
        User receiver = User.builder().userId(2).email("receiver@example.com").build();

        // Receiver가 알림 조건을 충족하는 ChatRoom 생성
        ChatRoom chatRoomWithReceiverNotification = ChatRoom.builder()
            .chatRoomId(1)
            .sender(sender)
            .receiver(receiver)
            .receiverUnreadCount(3) // 읽지 않은 메시지가 있음
            .senderLastMessageAt(now.minusDays(2)) // 마지막 메시지가 1일 이상 지남
            .build();

        // 알림 조건을 충족하지 않는 ChatRoom 생성
        ChatRoom chatRoomWithoutNotification = ChatRoom.builder()
            .chatRoomId(2)
            .sender(sender)
            .receiver(receiver)
            .receiverUnreadCount(0) // 읽지 않은 메시지가 없음
            .senderLastMessageAt(now.minusHours(12)) // 마지막 메시지가 1일 지나지 않음
            .build();

        when(chatRoomRepository.findAll())
            .thenReturn(List.of(chatRoomWithReceiverNotification, chatRoomWithoutNotification));

        // when
        chatService.notifyUnreadMessages();

        // then
        // Mocked repository가 호출되었는지 확인
        verify(chatRoomRepository, times(1)).findAll();

        // 알림 조건을 충족하는 데이터 검증
        assertEquals(3, chatRoomWithReceiverNotification.getReceiverUnreadCount());
        assertTrue(
            chatRoomWithReceiverNotification.getSenderLastMessageAt().isBefore(now.minusDays(1)));

        // 알림 조건을 충족하지 않는 데이터 검증
        assertEquals(0, chatRoomWithoutNotification.getReceiverUnreadCount());
        assertTrue(chatRoomWithoutNotification.getSenderLastMessageAt().isAfter(now.minusDays(1)));
    }

    @Test
    void sendWebSocketMessage_WhenMessageContentIsEmpty_ShouldNotSendMessage() {
        // given
        ChatMessageRequest messageRequest = ChatMessageRequest.builder()
            .content("") // 빈 메시지
            .roomId(1)
            .build();
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user@example.com");

        // Mock 객체 생성
        ChatService mockChatService = mock(ChatService.class);
        SimpMessageSendingOperations mockMessagingTemplate = mock(
            SimpMessageSendingOperations.class);

        // 테스트할 컨트롤러를 설정 (Mock 주입)
        ChatController chatController = new ChatController(mockChatService, mockMessagingTemplate);

        // when
        chatController.sendWebSocketMessage(messageRequest, principal);

        // then
        // ChatService와 MessagingTemplate가 호출되지 않았음을 검증
        verifyNoInteractions(mockChatService);
        verifyNoInteractions(mockMessagingTemplate);
    }


    @Test
    void sendWebSocketMessage_WhenMessageContentIsValid_ShouldSendMessage() {
        // given
        ChatMessageRequest messageRequest = ChatMessageRequest.builder()
            .content("Hello")
            .roomId(1)
            .build();
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user@example.com");

        User sender = User.builder()
            .userId(1)
            .email("user@example.com")
            .name("Test User")
            .build();
        User receiver = User.builder()
            .userId(2)
            .email("receiver@example.com")
            .name("Receiver User")
            .build();

        ChatRoom chatRoom = ChatRoom.builder()
            .chatRoomId(1)
            .sender(sender)
            .receiver(receiver)
            .build();

        ChatMessage chatMessage = ChatMessage.builder()
            .messageId(100)
            .content("Hello")
            .chatRoom(chatRoom)
            .sender(sender)
            .receiver(receiver) // receiver 설정
            .sendAt(LocalDateTime.now())
            .isRead(false)
            .build();

        ChatMessageResponse response = ChatMessageResponse.builder()
            .messageId(chatMessage.getMessageId())
            .content(chatMessage.getContent())
            .roomId(chatMessage.getChatRoom().getChatRoomId())
            .senderId(chatMessage.getSender().getUserId())
            .receiverId(chatMessage.getReceiver().getUserId()) // receiver 확인
            .sendAt(chatMessage.getSendAt())
            .build();

        // Mock 설정
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sender));
        when(chatRoomRepository.findById(1)).thenReturn(Optional.of(chatRoom));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(chatMessage);

        // when
        var result = chatService.sendMessage(messageRequest, "user@example.com", 1);

        // then
        assertNotNull(result);
        assertEquals("Hello", result.getContent());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }


    @Test
    void sendWebSocketMessage_WhenPrincipalIsNull_ShouldThrowException() {
        // given
        ChatMessageRequest messageRequest = ChatMessageRequest.builder()
            .content("Hello")
            .roomId(1)
            .build();

        // when & then
        assertThrows(NullPointerException.class, () -> {
            chatController.sendWebSocketMessage(messageRequest, null);
        });
    }
}