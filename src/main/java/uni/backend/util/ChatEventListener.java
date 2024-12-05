package uni.backend.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import uni.backend.service.ChatService;

import java.security.Principal;

@RequiredArgsConstructor
public class ChatEventListener {
    private final ChatService chatService;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();

        if (user != null) {
            String username = user.getName();
            System.out.println("User disconnected: " + username);

            Integer roomId = (Integer) headerAccessor.getSessionAttributes().get("roomId");
            if (roomId != null) {
                try {
                    // 메시지 읽음 처리 메서드 호출
                    chatService.markMessagesAsRead(roomId, username);
                    System.out.println("Marked all messages as read for user: " + username);
                } catch (Exception e) {
                    System.err.println("Failed to mark messages as read: " + e.getMessage());
                }
            }
        }
    }
}
