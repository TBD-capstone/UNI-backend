package uni.backend.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import org.springframework.messaging.handler.annotation.SendTo;
import uni.backend.domain.ChatMessage;

@Controller
public class ChatController {

    @MessageMapping("/chat/{roomId}")
    @SendTo("/sub/chat/{roomId}")
    public ChatMessage sendMessage(ChatMessage message) {
        return message;
    }
}
