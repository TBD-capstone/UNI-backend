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

    /*public List<ChatRoom> getChatRooms(User user) {
        return chatRoomRepository.findBySenderOrReceiver(user, user);
    }

    public ChatRoom createOrGetChatRoom(User sender, User receiver) {
        // 채팅방이 이미 존재하는지 확인
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findBySenderAndReceiver(sender, receiver);

        // 기존 채팅방이 있으면 그 채팅방을 반환
        if (existingChatRoom.isPresent()) {
            return existingChatRoom.get();
        }

        // 채팅방이 없으면 새 채팅방 생성 후 저장
        return createChatRoom(sender.getEmail(), );  // 데이터베이스에 저장
    }*/

    // 채팅방 생성
    @Transactional
    public ChatRoom createChatRoom(String senderEmail, ChatRoomRequest request) {
        // 이메일을 통해 발신자 찾기
        User sender = userRepository.findByEmail(senderEmail);
        // 수신자를 ID로 찾기, 없으면 예외 발생
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."));

        // 기존 채팅방이 있으면 그 채팅방을 반환
        if(chatRoomRepository.findBySenderAndReceiver(sender, receiver).isPresent()){
            return chatRoomRepository.findBySenderAndReceiver(sender, receiver).get();
        }

        // 새 채팅방 생성
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setSender(sender);
        chatRoom.setReceiver(receiver);
        chatRoom.setCreatedAt(request.getCreatedAt());

        // 채팅방 저장 후 반환
        return chatRoomRepository.save(chatRoom);
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
