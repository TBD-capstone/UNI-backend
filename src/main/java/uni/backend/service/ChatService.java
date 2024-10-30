package uni.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.ChatMessage;
import uni.backend.domain.ChatRoom;
import uni.backend.domain.User;
import uni.backend.domain.dto.ChatMessageRequest;
import uni.backend.domain.dto.ChatMessageResponse;
import uni.backend.domain.dto.ChatRoomRequest;
import uni.backend.repository.ChatMessageRepository;
import uni.backend.repository.ChatRoomRepository;
import uni.backend.repository.UserRepository;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    // 채팅방 생성
    @Transactional
    public ChatRoom createChatRoom(String senderEmail, ChatRoomRequest request) {
        User sender = userRepository.findByEmail(senderEmail);
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."));

        return chatRoomRepository.findBySenderAndReceiver(sender, receiver)
                .orElseGet(() -> {
                    ChatRoom chatRoom = new ChatRoom();
                    chatRoom.setSender(sender);
                    chatRoom.setReceiver(receiver);
                    chatRoom.setCreatedAt(request.getCreatedAt());
                    return chatRoomRepository.save(chatRoom);
                });
    }

    // Principal에서 유저의 이메일을 가져와 User ID 반환
    public Integer getSenderIdFromPrincipal(Principal principal) {
        User user = userRepository.findByEmail(principal.getName());
        if (user == null) {
            throw new IllegalStateException("User not found");
        }
        return user.getUserId();
    }

    @Transactional
    public ChatMessage sendMessage(ChatMessageRequest messageRequest) {
        ChatRoom chatRoom = chatRoomRepository.findById(messageRequest.getRoomId()).orElseThrow();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatRoom(chatRoom);
        chatMessage.setSender(userRepository.findById(messageRequest.getSenderId()).orElseThrow());
        chatMessage.setContent(messageRequest.getContent());
        chatMessage.setSendAt(LocalDateTime.now());

        return chatMessageRepository.save(chatMessage);
    }

    // 채팅방에 속한 메시지 조회
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatMessages(Integer roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid chat room ID"));

        return chatMessageRepository.findByChatRoom(chatRoom).stream()
                .map(message -> ChatMessageResponse.builder()
                        .content(message.getContent())
                        .senderId(message.getSender().getUserId())
                        .sendAt(message.getSendAt())
                        .build())
                .collect(Collectors.toList());
    }

    // 채팅방에 속한 메시지를 ChatMessageResponse 리스트로 반환
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatMessagesForRoom(Integer chatRoomId) {
        return getChatMessages(chatRoomId);
    }

    // 채팅방에서 상대방의 ID 반환
    @Transactional(readOnly = true)
    public Integer getOtherUserId(ChatRoom chatRoom, User currentUser) {
        return chatRoom.getSender().equals(currentUser) ?
                chatRoom.getReceiver().getUserId() :
                chatRoom.getSender().getUserId();
    }
}
