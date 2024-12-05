package uni.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.dto.*;
import uni.backend.service.ChatService;


import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessageSendingOperations messagingTemplate;

    // 채팅방 목록 조회
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getChatRooms(Principal principal) {
        List<ChatRoomResponse> chatRooms = chatService.getChatRoomsForUser(principal.getName());
        return ResponseEntity.ok(chatRooms);
    }

    // 채팅방 생성
    @PostMapping("/request")
    public ResponseEntity<ChatRoomResponse> requestChat(@RequestBody ChatRoomRequest request, Principal principal) {
        ChatRoomResponse chatRoomResponse = chatService.createChatRoom(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(chatRoomResponse);
    }

    // 채팅방 메시지 조회
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ChatMessageResponse>> getChatRoomMessages(@PathVariable Integer roomId) {
        List<ChatMessageResponse> chatMessages = chatService.getChatMessages(roomId);
        return ResponseEntity.ok(chatMessages);
    }

    // WebSocket으로 메시지 전송 처리
    @MessageMapping("/message")
    public void sendWebSocketMessage(@Payload ChatMessageRequest messageRequest, Principal principal) {
        if (messageRequest.getContent() == null || messageRequest.getContent().trim().isEmpty()) {
            return;
        }
        ChatMessageResponse response = chatService.sendMessage(messageRequest, principal.getName(), null);

        messagingTemplate.convertAndSend("/sub/chat/room/" + messageRequest.getRoomId(), response);
        messagingTemplate.convertAndSend("/sub/user/" + response.getReceiverId(), response);
    }

    // RESTful POST 요청으로 메시지 전송 처리
    @PostMapping("/room/{roomId}/messages")
    public ResponseEntity<ChatMessageResponse> sendRestMessage(
            @PathVariable Integer roomId,
            @RequestBody ChatMessageRequest messageRequest,
            Principal principal) {
        ChatMessageResponse response = chatService.sendMessage(messageRequest, principal.getName(), roomId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 메시지 번역
    @GetMapping("/translate/{messageId}")
    public ResponseEntity<String> translateChatMessage(@PathVariable Integer messageId, HttpServletRequest request) {
        String acceptLanguage = request.getHeader("Accept-Language");
        String translation = chatService.translateMessage(messageId, acceptLanguage);

        if (translation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Message or translation failed");
        }

        return ResponseEntity.ok(translation);
    }

    //개별 메시지 읽음 처리
    @MessageMapping("/read")
    public void handleIncomingMessage(@Payload ChatMessageRequest messageRequest, Principal principal) {
        ChatMessageResponse response = chatService.sendMessage(messageRequest, principal.getName(), null);

        messagingTemplate.convertAndSend("/sub/chat/room/" + messageRequest.getRoomId(), response);

        chatService.markMessageAsRead(response.getMessageId(), messageRequest.getRoomId(), principal.getName());
    }

    //특정 채팅방 메시지 읽음 처리
    @MessageMapping("/enter")
    public void markMessagesAsRead(@Payload Integer roomId, Principal principal) {
        chatService.markMessagesAsRead(roomId, principal.getName());
    }

    @PostMapping("/read/bulk")
    public ResponseEntity<String> handleBulkRead(@RequestBody ReadMessagesRequest request, Principal principal) {
        try {
            chatService.markMessagesAsRead(request.getRoomId(), request.getMessageIds(), principal.getName());
            return ResponseEntity.ok("Messages marked as read.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to mark messages as read.");
        }
    }

    @PostMapping("/room/{roomId}/leave")
    public ResponseEntity<String> leaveChatRoom(@PathVariable Integer roomId, Principal principal) {
        try {
            // 채팅방의 메시지 읽음 처리
            chatService.markMessagesAsRead(roomId, principal.getName());
            return ResponseEntity.ok("Successfully left the chat room.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to leave chat room.");
        }
    }
}