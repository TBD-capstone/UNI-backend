package uni.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.MainCategory;
import uni.backend.domain.Profile;
import uni.backend.domain.User;
import uni.backend.domain.dto.ProfileDTO;
import uni.backend.service.HashtagService;
import uni.backend.service.ProfileService;
import uni.backend.service.UserService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;
    private final HashtagService hashtagService;

    @GetMapping("/profile")
    public String viewProfile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        User user = userService.findByEmail(email);
        Optional<Profile> profile = profileService.findProfileByUserId(user.getUserId());

        if (profile.isEmpty()) {
            return "redirect:/profile-form";
        }

        model.addAttribute("profile", profile.get());
        model.addAttribute("remoteUser", email);

        return "profile";
    }

    @GetMapping("/profile-form")
    public String showProfileForm(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        User user = userService.findByEmail(email);
        Profile profile = new Profile();
        profile.setUser(user);
        model.addAttribute("profile", profile);

        return "profile-form";
    }

    @PostMapping("/profile")
    public String saveProfile(Profile profile, Authentication authentication, @RequestParam List<String> mainCategories) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        User user = userService.findByEmail(email);
        profile.setUser(user);
        profileService.save(profile);
        hashtagService.addHashtagsToProfile(profile, mainCategories);

        return "redirect:/profile";
    }

    @GetMapping("/profile-edit")
    public String editProfile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        User user = userService.findByEmail(email);
        Optional<Profile> profile = profileService.findProfileByUserId(user.getUserId());

        if (profile.isEmpty()) {
            return "redirect:/profile-form";
        }

        model.addAttribute("profile", profile.get());
        return "profile-edit";
    }

    @PostMapping("/profile-edit")
    public String updateProfile(@ModelAttribute ProfileDTO profileDTO,
                                Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        User user = userService.findByEmail(email);
        profileService.updateProfile(user.getUserId(), profileDTO); // mainCategories 제거

        return "redirect:/profile";
    }



    @GetMapping("/user/{user_id}")
    public ResponseEntity<ProfileDTO> getUserProfile(@PathVariable("user_id") Integer userId) {
        User user = userService.findById(userId);

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setUserId(user.getUserId());
        profileDTO.setImgProf(user.getProfile().getImgProf());
        profileDTO.setImgBack(user.getProfile().getImgBack());
        profileDTO.setUniv(user.getUniv());
        profileDTO.setRegion(user.getProfile().getRegion());
        profileDTO.setDescription(user.getProfile().getDescription());
        profileDTO.setNumEmployment(user.getProfile().getNumEmployment());
        profileDTO.setStar(user.getProfile().getStar());
        profileDTO.setTime(user.getProfile().getCreatedAt().toString()); // 생성 시간을 가져옵니다.

        return ResponseEntity.ok(profileDTO);
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<ProfileDTO> updateUserProfile(@PathVariable String userId, @RequestBody ProfileDTO profiledto) {
        Profile updatedProfile = profileService.updateProfile(Integer.valueOf(userId), profiledto);
        ProfileDTO profileDTO = profileService.getProfileDTOByUserId(Integer.valueOf(userId));
        return ResponseEntity.ok(profileDTO);
    }
}
