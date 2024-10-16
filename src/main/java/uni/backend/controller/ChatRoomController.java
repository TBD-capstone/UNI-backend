package uni.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChatRoomController {

    @GetMapping("/chat")
    public String chatRoom(@RequestParam String username, @RequestParam String roomId) {
        return "chat";
    }
}