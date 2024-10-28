package uni.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uni.backend.domain.Hashtag;
import uni.backend.domain.MainCategory;
import uni.backend.domain.Profile;
import uni.backend.domain.User;
import uni.backend.repository.HashtagRepository;
import uni.backend.repository.MainCategoryRepository;
import uni.backend.repository.ProfileRepository;
import uni.backend.repository.UserRepository;
import uni.backend.service.HashtagService;

import java.util.List;

@Controller
public class InputController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private MainCategoryRepository mainCategoryRepository;
    @Autowired
    private HashtagService hashtagService;

    @GetMapping("/input")
    public String inputForm() {
        return "input";
    }

    @PostMapping("/input")
    public String processInput(
            @RequestParam("userId") Integer userId,
            @RequestParam("hashtags") List<String> hashtags,
            Model model) {

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Profile profile = profileRepository.findByUser(user).orElseThrow(() -> new RuntimeException("User not found"));

        hashtagService.addHashtagsToProfile(profile, hashtags);

        model.addAttribute("message", "Hashtags successfully added");

        return "input";
    }

}
