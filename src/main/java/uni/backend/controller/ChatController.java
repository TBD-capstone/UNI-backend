package uni.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.CustomUserDetails;
import uni.backend.domain.dto.*;
import uni.backend.service.ChatService;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate; // SimpMessagingTemplate 주입

    @GetMapping("/chat/rooms")
    public String getChatRooms(Model model) {
        // 로그인된 사용자의 정보를 SecurityContextHolder에서 가져옵니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer myId = userDetails.getUserId(); // 사용자 ID를 가져옴
        List<ChatMainResponse> chatRooms = chatService.getChatRooms(myId); // 채팅방 목록 조회
        model.addAttribute("chatRooms", chatRooms);
        return "chat/main"; // 채팅방 목록 페이지로 연결
    }

    @GetMapping("/chat/room/{roomId}")
    public String getChatRoom(@PathVariable Integer roomId, Model model) {
        // roomId는 이미 Integer 타입이어야 하므로 이메일을 사용하지 않습니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer myId = userDetails.getUserId(); // 로그인한 사용자 ID 가져오기
        ChatRoomResponse chatRoom = chatService.getChatRoom(roomId, myId); // 채팅방 조회
        model.addAttribute("chatRoom", chatRoom);
        model.addAttribute("myId", myId);
        return "chat/room"; // 채팅방 페이지로 연결
    }

    @PostMapping("/chat/room")
    public String createChatRoom(@RequestParam("otherUserEmail") String otherUserEmail) {
        // 로그인된 사용자의 정보를 SecurityContextHolder에서 가져옵니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer myId = userDetails.getUserId(); // 로그인한 사용자 ID 가져오기
        chatService.createChatRoom(myId, otherUserEmail); // 이메일로 상대방과 채팅방 생성
        return "redirect:/chat/rooms"; // 채팅방 목록으로 리다이렉트
    }

    @MessageMapping("/sendMessage/{roomId}")
    public void sendMessage(@PathVariable Integer roomId, @RequestBody ChatMessageRequest messageRequest, Principal principal) {
        Integer senderId = ((CustomUserDetails) ((Authentication) principal).getPrincipal()).getUserId();
        chatService.sendMessage(roomId, senderId, messageRequest);
        // 메시지를 해당 채팅방에 연결된 클라이언트로 전달
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, messageRequest);
    }
}
