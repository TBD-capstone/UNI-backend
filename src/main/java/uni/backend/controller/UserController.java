package uni.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import uni.backend.domain.User;
import uni.backend.domain.Role;
import uni.backend.service.UserService;
import uni.backend.controller.UserForm;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/new")
    public String createUserForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        return "users/createUserForm";
    }

    @PostMapping("/users/new")
    public String signUp(UserForm form) {
        User user = new User();
        user.setName(form.getName());
        user.setEmail(form.getEmail());
        user.setPassword(form.getPassword());
        user.setRole(form.getRole());
        user.setStatus("INACTIVE");

        userService.signUp(user);
        return "redirect:/";
    }
}
