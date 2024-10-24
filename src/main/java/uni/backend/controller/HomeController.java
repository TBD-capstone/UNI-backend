package uni.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import uni.backend.domain.User;
import uni.backend.service.UserService;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        User user = userService.findByEmail(request.getRemoteUser());
        if (user != null) {
            model.addAttribute("remoteUser", user.getName());
            model.addAttribute("role", user.getRole().name());  // 역할 전달
        }
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";  // login.html 템플릿 반환
    }
}
