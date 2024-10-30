package uni.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import uni.backend.domain.Profile;
import uni.backend.domain.User;
import uni.backend.service.ProfileService;
import uni.backend.service.UserService;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;

    @GetMapping("/profile")
    public String viewProfile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        try {
            User user = userService.findByEmail(email);
            Profile profile = profileService.findProfileByUserId(user.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("프로필 정보가 없습니다."));
            model.addAttribute("profile", profile);
            model.addAttribute("remoteUser", email);
        } catch (IllegalArgumentException e) {
            return "redirect:/login";
        }

        return "profile";
    }

}
