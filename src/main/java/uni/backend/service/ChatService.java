package uni.backend.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.*;
import uni.backend.domain.dto.*;
import uni.backend.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public List<ChatMainResponse> getChatRooms(Integer myId) {
        User user = userRepository.findById(myId).orElseThrow(); // 유저 정보 조회
        // 유저가 user1이거나 user2인 모든 채팅방을 가져옴
        List<ChatRoom> chatRooms = chatRoomRepository.findByUser1OrUser2(user, user);
        return chatRooms.stream()
                .map(chatRoom -> ChatMainResponse.builder()
                        .chatRoomId(chatRoom.getChatRoomId())
                        .otherName(chatRoom.getOtherUser(myId).getName())
                        .lastMessageAt(chatRoom.getLastMessageAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatRoomResponse getChatRoom(Integer roomId, Integer myId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow();
        List<ChatMessageResponse> messages = chatRoom.getMessages().stream()
                .map(message -> ChatMessageResponse.builder()
                        .content(message.getContent())
                        .senderId(message.getSender().getUserId())
                        .sendAt(message.getSendAt())
                        .build())
                .collect(Collectors.toList());

        return ChatRoomResponse.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .myId(myId)
                .otherId(chatRoom.getOtherUser(myId).getUserId())
                .chatMessages(messages)
                .build();
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void sendMessage(Integer roomId, Integer senderId, ChatMessageRequest messageRequest) {
        System.out.println("sendMessage called with roomId: " + roomId + ", senderId: " + senderId);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        System.out.println("ChatRoom and User found: " + chatRoom.getChatRoomId() + ", " + sender.getUserId());

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(messageRequest.getContent())
                .sendAt(LocalDateTime.now())
                .build();

        System.out.println("Saving chat message: " + chatMessage.getContent());

        chatMessageRepository.save(chatMessage);  // 메시지 저장
        chatMessageRepository.flush();  // DB에 즉시 반영

        System.out.println("Chat message saved successfully");
    }

    @Transactional
    public void createChatRoom(Integer myId, String otherUserEmail) {
        // 로그인된 사용자와 상대방 사용자를 찾습니다.
        User myUser = userRepository.findById(myId).orElseThrow();
        User otherUser = userRepository.findByEmail(otherUserEmail); // 이메일로 상대방 조회

        if (myUser.getRole() != Role.KOREAN) {
            throw new IllegalStateException("한국 대학생만 채팅방을 생성할 수 있습니다.");
        }

        // user1과 user2 또는 user2와 user1으로 된 채팅방을 찾기
        chatRoomRepository.findByUser1AndUser2(myUser, otherUser)
                .or(() -> chatRoomRepository.findByUser2AndUser1(myUser, otherUser))
                .ifPresentOrElse(
                        room -> {
                            // 이미 채팅방이 존재할 경우 처리
                        },
                        () -> {
                            // 새로운 채팅방 생성
                            ChatRoom chatRoom = ChatRoom.builder()
                                    .user1(myUser)
                                    .user2(otherUser)
                                    .createdAt(LocalDateTime.now())
                                    .build();
                            chatRoomRepository.save(chatRoom);
                        }
                );
    }
}
