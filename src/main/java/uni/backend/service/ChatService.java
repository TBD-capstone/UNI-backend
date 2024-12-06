package uni.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.ChatMessage;
import uni.backend.domain.ChatRoom;
import uni.backend.domain.User;
import uni.backend.domain.dto.*;
import uni.backend.repository.ChatMessageRepository;
import uni.backend.repository.ChatRoomRepository;
import uni.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final TranslationService translationService;
    private final UserStatusScheduler userStatusScheduler;

    // 채팅방 생성
    @Transactional
    public ChatRoomResponse createChatRoom(String senderEmail, ChatRoomRequest request) {
        User sender = findUserByEmail(senderEmail);
        User receiver = findUserById(request.getReceiverId());

        ChatRoom chatRoom = chatRoomRepository.findBySenderAndReceiver(sender, receiver)
                .orElseGet(() -> chatRoomRepository.save(
                        ChatRoom.builder()
                                .sender(sender)
                                .receiver(receiver)
                                .build()
                ));

        return toChatRoomResponse(chatRoom, sender);
    }

    // 메시지 전송
    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request, String senderEmail, Integer roomId) {
        if (request.getContent() == null || request.getContent().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be null or empty");
        }
        User sender = findUserByEmail(senderEmail);
        ChatRoom chatRoom = findChatRoomById(roomId != null ? roomId : request.getRoomId());
        User receiver = findReceiver(chatRoom, sender);

        ChatMessage message = chatMessageRepository.save(
                ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender(sender)
                        .receiver(receiver)
                        .content(request.getContent())
                        .sendAt(LocalDateTime.now())
                        .isRead(false)
                        .build()
        );

        // 마지막 메시지 시간 갱신
        if (chatRoom.getSender().equals(sender)) {
            chatRoom.setSenderLastMessageAt(LocalDateTime.now());
            chatRoom.setReceiverUnreadCount(chatRoom.getReceiverUnreadCount() + 1);
        } else {
            chatRoom.setReceiverLastMessageAt(LocalDateTime.now());
            chatRoom.setSenderUnreadCount(chatRoom.getSenderUnreadCount() + 1);
        }
        chatRoomRepository.save(chatRoom);

        return toChatMessageResponse(message);
    }

    //특정 채팅방 메시지 읽음 처리
    @Transactional
    public void markMessagesAsRead(Integer roomId, String username) {
        ChatRoom chatRoom = findChatRoomById(roomId);
        User receiver = findUserByEmail(username);

        List<ChatMessage> unreadMessages = chatRoom.getChatMessages().stream()
                .filter(msg -> !msg.isRead() && msg.getReceiver().equals(receiver))
                .collect(Collectors.toList());

        unreadMessages.forEach(msg -> msg.setRead(true));

        if (chatRoom.getSender().equals(receiver)) {
            chatRoom.setSenderUnreadCount(0);
        } else if (chatRoom.getReceiver().equals(receiver)) {
            chatRoom.setReceiverUnreadCount(0);
        }

        chatMessageRepository.saveAll(unreadMessages);
    }

    // 채팅방 조회
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getChatRoomsForUser(String email) {
        User user = findUserByEmail(email);
        List<ChatRoom> chatRooms = chatRoomRepository.findBySenderOrReceiver(user, user);

        return chatRooms.stream()
                .map(room -> toChatRoomResponse(room, user))
                .collect(Collectors.toList());
    }

    // 메시지 조회
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatMessages(Integer roomId) {
        ChatRoom chatRoom = findChatRoomById(roomId);
        return chatMessageRepository.findByChatRoom(chatRoom).stream()
                .map(this::toChatMessageResponse)
                .collect(Collectors.toList());
    }

    // 메시지 번역
    public String translateMessage(Integer messageId, String acceptLanguage) {
        String targetLanguage = translationService.determineTargetLanguage(acceptLanguage);
        String originalMessage = getMessageById(messageId);

        if (originalMessage == null) {
            return null;
        }

        TranslationRequest translationRequest = new TranslationRequest();
        translationRequest.setText(List.of(originalMessage));
        translationRequest.setTarget_lang(targetLanguage);
        TranslationResponse response = translationService.translate(translationRequest);

        if (response == null || response.getTranslations().isEmpty()) {
            return null;
        }

        return response.getTranslations().getFirst().getText();
    }

    // Helper Methods
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    private User findUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    private ChatRoom findChatRoomById(Integer roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found with ID: " + roomId));
    }

    public String getMessageById(Integer messageId) {
        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message with ID " + messageId + " not found"));
        return chatMessage.getContent();
    }

    private User findReceiver(ChatRoom chatRoom, User sender) {
        return chatRoom.getSender().equals(sender) ? chatRoom.getReceiver() : chatRoom.getSender();
    }

    private ChatRoomResponse toChatRoomResponse(ChatRoom chatRoom, User currentUser) {
        User otherUser = findReceiver(chatRoom, currentUser);

        List<ChatMessage> messages = chatRoom.getChatMessages() != null
                ? chatRoom.getChatMessages()
                : new ArrayList<>();

        long unreadCount = messages.stream()
                .filter(msg -> !msg.isRead() && msg.getReceiver().equals(currentUser))
                .count();
        if (chatRoom.getSender().equals(currentUser)) {
            chatRoom.setSenderUnreadCount(unreadCount);
        } else if (chatRoom.getReceiver().equals(currentUser)) {
            chatRoom.setReceiverUnreadCount(unreadCount);
        }

        return ChatRoomResponse.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .chatMessages(getChatMessages(chatRoom.getChatRoomId()))
                .myId(currentUser.getUserId())
                .myName(currentUser.getName())
                .myImgProf(currentUser.getProfile() != null ? currentUser.getProfile().getImgProf() : null)
                .otherId(otherUser.getUserId())
                .otherName(otherUser.getName())
                .otherImgProf(otherUser.getProfile() != null ? otherUser.getProfile().getImgProf() : null)
                .unreadCount(unreadCount)
                .build();
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .messageId(message.getMessageId())
                .roomId(message.getChatRoom().getChatRoomId())
                .content(message.getContent())
                .senderId(message.getSender().getUserId())
                .receiverId(message.getReceiver().getUserId())
                .sendAt(message.getSendAt())
                .build();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void notifyUnreadMessages() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();

        for (ChatRoom chatRoom : chatRooms) {
            LocalDateTime now = LocalDateTime.now();

            // Sender 가 읽지 않은 메시지가 있고, 마지막 메시지가 1일 이상 지난 경우
            if (chatRoom.getSenderUnreadCount() > 0 &&
                    chatRoom.getReceiverLastMessageAt() != null &&
                    chatRoom.getReceiverLastMessageAt().isBefore(now.minusDays(1))) {
                sendUnreadMessageNotification(chatRoom.getSender().getEmail());
            }
            // Receiver 가 읽지 않은 메시지가 있고, 마지막 메시지가 1일 이상 지난 경우
            if (chatRoom.getReceiverUnreadCount() > 0 &&
                    chatRoom.getSenderLastMessageAt() != null &&
                    chatRoom.getSenderLastMessageAt().isBefore(now.minusDays(1))) {
                sendUnreadMessageNotification(chatRoom.getReceiver().getEmail());
            }
        }
    }

    private void sendUnreadMessageNotification(String email) {
        userStatusScheduler.sendEmailNotification(email, "읽지 않은 메시지가 있습니다.");
    }

}