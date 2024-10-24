package uni.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.ChatRoom;
import uni.backend.domain.User;
import uni.backend.domain.dto.ChatMessageRequest;
import uni.backend.domain.dto.ChatRoomRequest;
import uni.backend.repository.ChatRoomRepository;
import uni.backend.service.ChatService;
import uni.backend.service.UserService;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    // 채팅방 목록 조회
    @GetMapping("/chat/rooms")
    public String getChatRooms(Principal principal, Model model) {
        User currentUser = userService.findByEmail(principal.getName());
        List<ChatRoom> chatRooms = chatRoomRepository.findBySenderOrReceiver(currentUser, currentUser);
        model.addAttribute("chatRooms", chatRooms);
        return "chat/roomList";
    }

    // 채팅방 생성 및 채팅 요청
    @PostMapping("/chat/request")
    public String requestChat(@ModelAttribute ChatRoomRequest request, Principal principal) {
        // 현재 로그인된 유저의 이메일을 통해 채팅방 생성
        ChatRoom chatRoom = chatService.createChatRoom(principal.getName(), request);

        // 생성된 채팅방으로 리다이렉트
        return "redirect:/chat/room/" + chatRoom.getChatRoomId();
    }

    // 채팅방 조회
    @GetMapping("/chat/room/{roomId}")
    public String getChatRoom(@PathVariable Integer roomId, Model model, Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());
        model.addAttribute("myId", currentUser.getUserId());  // 유저 ID 전달
        model.addAttribute("chatRoomId", roomId);  // 채팅방 ID 전달
        model.addAttribute("chatMessages", chatService.getChatMessages(roomId));
        return "chat/chatroom";
    }

    // 클라이언트가 /pub/chat/message로 메시지를 보낼 때 처리
    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageRequest messageRequest, Principal principal) {
        // 이메일을 통해 발신자 유저를 조회
        User sender = userService.findByEmail(principal.getName());

        // senderId를 이메일이 아닌 고유 유저 ID로 설정
        messageRequest.setSenderId(sender.getUserId());

        // 메시지 저장
        chatService.sendMessage(messageRequest);

        // 해당 채팅방 구독자에게 메시지 브로드캐스트
        messagingTemplate.convertAndSend("/sub/chat/room/" + messageRequest.getRoomId(), messageRequest);
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
