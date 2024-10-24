package uni.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.User;
import uni.backend.domain.dto.ChatMainResponse;
import uni.backend.domain.dto.ChatMessageRequest;
import uni.backend.domain.dto.ChatRoomRequest;
import uni.backend.service.ChatService;
import uni.backend.service.UserService;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final SimpMessageSendingOperations messagingTemplate;

    // 채팅방 목록 조회
    @GetMapping("/chat/rooms")
    public String getChatRooms(Principal principal, Model model) {
        // 이메일로 User 객체 조회
        User currentUser = userService.findByEmail(principal.getName());
        model.addAttribute("chatRooms", chatService.getChatRooms(currentUser));
        return "chat/chatrooms";
    }

    // 채팅방 생성
    @PostMapping("/chat/room")
    public String createChatRoom(@ModelAttribute ChatRoomRequest request, Principal principal) {
        chatService.createChatRoom(principal.getName(), request);
        return "redirect:/chat/rooms";
    }

    // 채팅방 조회
    @GetMapping("/chat/room/{roomId}")
    public String getChatRoom(@PathVariable Integer roomId, Principal principal, Model model) {
        model.addAttribute("chatRoomId", roomId);
        model.addAttribute("chatMessages", chatService.getChatMessages(roomId));
        model.addAttribute("myId", userService.findByEmail(principal.getName()).getUserId());
        return "chat/chatroom";
    }

    // 클라이언트가 /pub/chat/message로 메시지를 보낼 때 처리
    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageRequest messageRequest) {
        // 메시지를 저장
        chatService.sendMessage(messageRequest);

        // 해당 채팅방 구독자에게 메시지 브로드캐스트
        messagingTemplate.convertAndSend("/sub/chat/room/" + messageRequest.getRoomId(), messageRequest);
        System.out.println("메시지 브로드캐스트: " + messageRequest.getContent());  // 메시지 브로드캐스트 확인 로그
    }

    // POST 요청으로 메시지 전송 처리
    @PostMapping("/sendMessage/{roomId}")
    public String sendChatMessage(@PathVariable Integer roomId, @RequestParam String message, Principal principal) {
        ChatMessageRequest chatMessageRequest = new ChatMessageRequest();
        chatMessageRequest.setRoomId(roomId);
        chatMessageRequest.setContent(message);
        chatMessageRequest.setSenderId(chatService.getSenderIdFromPrincipal(principal));

        chatService.sendMessage(chatMessageRequest);
        return "redirect:/chat/room/" + roomId;
    }
}
