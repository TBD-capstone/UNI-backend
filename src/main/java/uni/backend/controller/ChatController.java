package uni.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
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
import uni.backend.domain.dto.*;
import uni.backend.repository.ChatRoomRepository;
import uni.backend.service.ChatService;
import uni.backend.service.TranslationService;
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
    private final TranslationService translationService;

    // 채팅방 목록 조회
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getChatRooms(Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());
        List<ChatRoom> chatRooms = chatRoomRepository.findBySenderOrReceiver(currentUser, currentUser);

        List<ChatRoomResponse> chatRoomResponses = chatRooms.stream()
                .map(chatRoom -> ChatRoomResponse.builder()
                        .chatRoomId(chatRoom.getChatRoomId())
                        .myId(currentUser.getUserId())
                        .otherId(chatService.getOtherUserId(chatRoom, currentUser))
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
                .otherId(chatService.getOtherUserId(chatRoom, currentUser))
                .chatMessages(chatService.getChatMessagesForRoom(chatRoom.getChatRoomId()))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(chatRoomResponse);
    }

    // 특정 채팅방 조회
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ChatMessageResponse>> getChatRoomMessages(@PathVariable Integer roomId, Principal principal) {
        List<ChatMessageResponse> chatMessages = chatService.getChatMessages(roomId);
        return ResponseEntity.ok(chatMessages);
    }

    // 클라이언트가 /pub/chat/message로 메시지를 보낼 때 처리
    @MessageMapping("/message")
    public void sendMessage(ChatMessageRequest messageRequest, Principal principal) {
        User sender = userService.findByEmail(principal.getName());
        messageRequest.setSenderId(sender.getUserId());
        ChatRoom chatRoom = chatRoomRepository.findById(messageRequest.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        Integer receiverId = chatRoom.getSender().getUserId().equals(messageRequest.getSenderId()) ?
                chatRoom.getReceiver().getUserId() : chatRoom.getSender().getUserId();
        messageRequest.setReceiverId(receiverId);

        // 디버그 로그: 메시지 수신
        logger.info("Received message from user: {}", sender.getUserId());

        // 메시지 저장
        ChatMessage savedMessage = chatService.sendMessage(messageRequest);
        logger.info("Saved message: {}", savedMessage);

        // 클라이언트로 메시지 전송
        ChatMessageResponse response = ChatMessageResponse.builder()
                .messageId(savedMessage.getMessageId())
                .content(savedMessage.getContent())
                .senderId(savedMessage.getSender().getUserId())
                .receiverId(savedMessage.getReceiver().getUserId())
                .sendAt(savedMessage.getSendAt())
                .build();

        messagingTemplate.convertAndSend("/sub/chat/room/" + messageRequest.getRoomId(), response);
        logger.info("Message sent to clients: {}", response);

        messagingTemplate.convertAndSend("/sub/user/" + messageRequest.getReceiverId(), response);
    }

    // RESTful POST 요청으로 메시지 전송 처리
    @PostMapping("/room/{roomId}/messages")
    public ResponseEntity<ChatMessageResponse> sendChatMessage(
            @PathVariable Integer roomId,
            @RequestBody ChatMessageRequest chatMessageRequest,
            Principal principal) {

        chatMessageRequest.setRoomId(roomId);
        chatMessageRequest.setSenderId(chatService.getSenderIdFromPrincipal(principal));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        Integer receiverId = chatRoom.getSender().getUserId().equals(chatMessageRequest.getSenderId()) ?
                chatRoom.getReceiver().getUserId() : chatRoom.getSender().getUserId();
        chatMessageRequest.setReceiverId(receiverId);

        ChatMessage savedMessage = chatService.sendMessage(chatMessageRequest);

        ChatMessageResponse response = ChatMessageResponse.builder()
                .messageId(savedMessage.getMessageId())
                .content(savedMessage.getContent())
                .senderId(savedMessage.getSender().getUserId())
                .receiverId(savedMessage.getReceiver().getUserId())
                .sendAt(savedMessage.getSendAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);  // HTTP 201 Created 상태와 함께 응답 반환
    }

    // 메시지 번역
    @GetMapping("/translate/{messageId}")
    public ResponseEntity<String> translateChatMessage(@PathVariable Integer messageId, HttpServletRequest request) {

        String acceptLanguage = request.getHeader("Accept-Language");

        String targetLanguage = translationService.determineTargetLanguage(acceptLanguage);

        String originalMessage = chatService.getMessageById(messageId);
        if (originalMessage == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Message not found");
        }

        TranslationRequest translationRequest = new TranslationRequest();
        translationRequest.setText(List.of(originalMessage));
        translationRequest.setTarget_lang(targetLanguage);

        TranslationResponse translationResponse = translationService.translate(translationRequest);
        if (translationResponse == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Translation failed");
        }

        String translatedText = translationResponse.getTranslations().get(0).getText();

        return ResponseEntity.ok(translatedText);
    }
}