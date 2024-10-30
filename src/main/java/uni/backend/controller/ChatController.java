package uni.backend.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.ChatMessage;
import uni.backend.domain.ChatRoom;
import uni.backend.domain.User;
import uni.backend.domain.dto.ChatMessageRequest;
import uni.backend.domain.dto.ChatMessageResponse;
import uni.backend.domain.dto.ChatRoomRequest;
import uni.backend.domain.dto.ChatRoomResponse;
import uni.backend.repository.ChatRoomRepository;
import uni.backend.service.ChatService;
import uni.backend.service.UserService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;
    private final UserService userService;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    // 채팅방 목록 조회
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getChatRooms(Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());
        List<ChatRoom> chatRooms = chatRoomRepository.findBySenderOrReceiver(currentUser, currentUser);

        List<ChatRoomResponse> chatRoomResponses = chatRooms.stream()
                .map(chatRoom -> ChatRoomResponse.builder()
                        .chatRoomId(chatRoom.getChatRoomId())
                        .myId(currentUser.getUserId())
                        .otherId(chatService.getOtherUserId(chatRoom, currentUser)) // 다른 사용자 ID
                        .chatMessages(chatService.getChatMessagesForRoom(chatRoom.getChatRoomId()))
                        .build())
                .toList();

        return ResponseEntity.ok(chatRoomResponses);
    }

    // 채팅방 생성 및 채팅 요청
    @PostMapping("/request")
    public ResponseEntity<ChatRoomResponse> requestChat(@RequestBody ChatRoomRequest request, Principal principal) {
        ChatRoom chatRoom = chatService.createChatRoom(principal.getName(), request);

        User currentUser = userService.findByEmail(principal.getName());
        ChatRoomResponse chatRoomResponse = ChatRoomResponse.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .myId(currentUser.getUserId())
                .otherId(chatService.getOtherUserId(chatRoom, currentUser)) // 다른 사용자 ID
                .chatMessages(chatService.getChatMessagesForRoom(chatRoom.getChatRoomId()))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(chatRoomResponse);
    }

    // 특정 채팅방 조회
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ChatMessageResponse>> getChatRoomMessages(@PathVariable Integer roomId, Principal principal) {
        List<ChatMessageResponse> chatMessages = chatService.getChatMessages(roomId);
        return ResponseEntity.ok(chatMessages); // HTTP 200 OK 상태와 함께 메시지 목록 반환
    }


    // 클라이언트가 /pub/chat/message로 메시지를 보낼 때 처리
    @MessageMapping("/message")
    public void sendMessage(ChatMessageRequest messageRequest, Principal principal) {
        User sender = userService.findByEmail(principal.getName());
        messageRequest.setSenderId(sender.getUserId());

        chatService.sendMessage(messageRequest);
        messagingTemplate.convertAndSend("/sub/chat/room/" + messageRequest.getRoomId(), messageRequest);
    }

    // RESTful POST 요청으로 메시지 전송 처리
    @PostMapping("/room/{roomId}/messages")
    public ResponseEntity<ChatMessageResponse> sendChatMessage(
            @PathVariable Integer roomId,
            @RequestBody ChatMessageRequest chatMessageRequest,
            Principal principal) {

        chatMessageRequest.setRoomId(roomId);
        chatMessageRequest.setSenderId(chatService.getSenderIdFromPrincipal(principal));

        ChatMessage savedMessage = chatService.sendMessage(chatMessageRequest);

        ChatMessageResponse response = ChatMessageResponse.builder()
                .content(savedMessage.getContent())
                .senderId(savedMessage.getSender().getUserId())
                .sendAt(savedMessage.getSendAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);  // HTTP 201 Created 상태와 함께 응답 반환
    }



}
