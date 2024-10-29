package uni.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import uni.backend.domain.User;
import uni.backend.service.UserService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/users/new")
    public String createUserForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        return "users/createUserForm";
    }

    @PostMapping("/users/new")
    public String newUser(@Valid UserForm form, BindingResult bindingResult, Model model) {

        if(bindingResult.hasErrors()) {
            return "users/createUserForm";
        }

        try{
            User user = User.createUser(form, passwordEncoder);
            userService.saveUser(user);
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "users/createUserForm";
        }

        return "redirect:/";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers()); // 모든 회원을 조회하여 모델에 추가
        return "users/list"; // 회원 목록 페이지로 이동
    }

    @GetMapping("/users/list")
    public String userList(Model model) {
        // ROLE이 KOREAN인 사용자들만 가져옴
        List<User> koreanUsers = userService.findKoreanUsers();
        model.addAttribute("koreanUsers", koreanUsers);
        return "users/userList";
    }
}
