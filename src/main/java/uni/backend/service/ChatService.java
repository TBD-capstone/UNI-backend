package uni.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.ChatMessage;
import uni.backend.domain.ChatRoom;
import uni.backend.domain.User;
import uni.backend.domain.dto.*;
import uni.backend.repository.ChatMessageRepository;
import uni.backend.repository.ChatRoomRepository;
import uni.backend.repository.UserRepository;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    // 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<ChatMainResponse> getChatRooms(User user) {
        // sender 또는 receiver가 현재 사용자와 관련된 모든 채팅방 조회
        List<ChatRoom> chatRooms = chatRoomRepository.findBySenderOrReceiver(user, user);

        return chatRooms.stream()
                .map(room -> ChatMainResponse.builder()
                        .chatRoomId(room.getChatRoomId())
                        // 메시지가 있는지 확인 후 처리
                        .otherName(room.getReceiver().getName())
                        .lastMessageTime(room.getChatMessages().isEmpty() ? null : room.getChatMessages().get(0).getSendAt())
                        .build())
                .collect(Collectors.toList());
    }

    // 채팅방 생성
    @Transactional
    public void createChatRoom(String senderEmail, ChatRoomRequest request) {
        User sender = userRepository.findByEmail(senderEmail);
        User receiver = userRepository.findById(request.getReceiverId()).orElseThrow();

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setSender(sender);
        chatRoom.setReceiver(receiver);
        chatRoom.setCreatedAt(request.getCreatedAt());

        chatRoomRepository.save(chatRoom);
    }

    // Principal에서 유저의 이메일을 가져와 User ID 반환
    public Integer getSenderIdFromPrincipal(Principal principal) {
        String email = principal.getName(); // Principal에서 이메일을 가져옴
        User user = userRepository.findByEmail(email); // 이메일을 통해 유저 조회
        if (user == null) {
            throw new IllegalStateException("User not found");
        }
        return user.getUserId(); // 유저의 ID 반환
    }

    @Transactional
    public void sendMessage(ChatMessageRequest messageRequest) {
        ChatRoom chatRoom = chatRoomRepository.findById(messageRequest.getRoomId()).orElseThrow();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatRoom(chatRoom);
        chatMessage.setSender(userRepository.findById(messageRequest.getSenderId()).orElseThrow());
        chatMessage.setContent(messageRequest.getContent());
        chatMessage.setSendAt(LocalDateTime.now());
        chatMessageRepository.save(chatMessage);
    }

    // 채팅방에 속한 메시지 조회
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatMessages(Integer roomId) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Invalid chat room ID"));

        // 채팅방에 속한 메시지들을 가져옴
        List<ChatMessage> messages = chatMessageRepository.findByChatRoom(chatRoom);

        // 메시지를 ChatMessageResponse로 변환하여 반환
        return messages.stream()
                .map(message -> ChatMessageResponse.builder()
                        .content(message.getContent())
                        .senderId(message.getSender().getUserId())
                        .sendAt(message.getSendAt())
                        .build())
                .collect(Collectors.toList());
    }
}
