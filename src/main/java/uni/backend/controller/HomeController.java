package uni.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        model.addAttribute("remoteUser", request.getRemoteUser());
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";  // login.html 템플릿 반환
    }
}
