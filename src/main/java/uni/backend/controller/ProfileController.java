package uni.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.Profile;
import uni.backend.domain.User;
import uni.backend.domain.dto.IndividualProfileResponse;
import uni.backend.service.HashtagService;
import uni.backend.service.ProfileService;
import uni.backend.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;
    private final HashtagService hashtagService;

//    @GetMapping("/profile")
//    public String viewProfile(Model model, Authentication authentication) {
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return "redirect:/login";
//        }
//
//        String email = authentication.getName();
//        User user = userService.findByEmail(email);
//        Optional<Profile> profile = profileService.findProfileByUserId(user.getUserId());
//
//        if (profile.isEmpty()) {
//            return "redirect:/profile-form";
//        }
//
//        model.addAttribute("profile", profile.get());
//        model.addAttribute("remoteUser", email);
//
//        return "profile";
//    }
//
//    @GetMapping("/profile-form")
//    public String showProfileForm(Model model, Authentication authentication) {
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return "redirect:/login";
//        }
//
//        String email = authentication.getName();
//        User user = userService.findByEmail(email);
//        Profile profile = new Profile();
//        profile.setUser(user);
//        model.addAttribute("profile", profile);
//
//        return "profile-form";
//    }
//
//    @PostMapping("/profile")
//    public String saveProfile(Profile profile, Authentication authentication, @RequestParam List<String> mainCategories) {
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return "redirect:/login";
//        }
//
//        String email = authentication.getName();
//        User user = userService.findByEmail(email);
//        profile.setUser(user);
//        profileService.save(profile);
//        hashtagService.addHashtagsToProfile(profile, mainCategories);
//
//        return "redirect:/profile";
//    }
//
//    @GetMapping("/profile-edit")
//    public String editProfile(Model model, Authentication authentication) {
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return "redirect:/login";
//        }
//
//        String email = authentication.getName();
//        User user = userService.findByEmail(email);
//        Optional<Profile> profile = profileService.findProfileByUserId(user.getUserId());
//
//        if (profile.isEmpty()) {
//            return "redirect:/profile-form";
//        }
//
//        model.addAttribute("profile", profile.get());
//        return "profile-edit";
//    }
//
//    @PostMapping("/profile-edit")
//    public String updateProfile(@ModelAttribute ProfileDTO profileDTO,
//                                Authentication authentication) {
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return "redirect:/login";
//        }
//
//        String email = authentication.getName();
//        User user = userService.findByEmail(email);
//        profileService.updateProfile(user.getUserId(), profileDTO); // mainCategories 제거
//
//        return "redirect:/profile";
//    }


    @GetMapping("/user/{user_id}")
    public ResponseEntity<IndividualProfileResponse> getUserProfile(
        @PathVariable("user_id") Integer userId) {
        User user = userService.findById(userId);
        IndividualProfileResponse individualProfileResponse = new IndividualProfileResponse();

        individualProfileResponse.setUserId(user.getUserId());
        individualProfileResponse.setImgProf(user.getProfile().getImgProf());
        individualProfileResponse.setImgBack(user.getProfile().getImgBack());
        individualProfileResponse.setUniv(user.getUnivName());
        individualProfileResponse.setRegion(user.getProfile().getRegion());
        individualProfileResponse.setDescription(user.getProfile().getDescription());
        individualProfileResponse.setNumEmployment(user.getProfile().getNumEmployment());
        individualProfileResponse.setStar(user.getProfile().getStar());
        individualProfileResponse.setTime(
            user.getProfile().getCreatedAt().toString()); // 생성 시간을 가져옵니다.

        // 해시태그 목록을 String으로 설정
        List<String> hashtags = user.getProfile().getMainCategories().stream()
            .map(mainCategory -> mainCategory.getHashtag().getHashtagName())
            .collect(Collectors.toList());
        individualProfileResponse.setHashtags(hashtags);

        return ResponseEntity.ok(individualProfileResponse);
    }


    @PostMapping("/user/{userId}")
    public ResponseEntity<IndividualProfileResponse> updateUserProfile(@PathVariable String userId,
        @RequestBody IndividualProfileResponse profiledto) {
        Profile updatedProfile = profileService.updateProfile(Integer.valueOf(userId), profiledto);
        IndividualProfileResponse individualProfileResponse = profileService.getProfileDTOByUserId(
            Integer.valueOf(userId));
        return ResponseEntity.ok(individualProfileResponse);
    }

}
