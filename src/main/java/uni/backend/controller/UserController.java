package uni.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uni.backend.domain.User;
import uni.backend.domain.dto.UserForm;
import uni.backend.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

/*    @GetMapping("/users/new")
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
    }*/

    @PostMapping("/users/new")
    public ResponseEntity<?> registerUser(@RequestBody @Valid UserForm form) {
        try {
            // 새로운 사용자 생성
            User user = User.createUser(form, passwordEncoder);
            userService.saveUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");

        } catch (IllegalStateException e) {
            // 중복 사용자 오류 등의 예외 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("오류: " + e.getMessage());
        }
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
