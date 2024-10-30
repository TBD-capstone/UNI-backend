package uni.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uni.backend.service.HashtagService;

import java.util.Arrays;
import java.util.List;

@Controller
public class HashtagController {

    @Autowired
    private HashtagService hashtagService;

    @GetMapping("/search")
    public String showSearchPage() {
        return "search";
    }

    @GetMapping("/search-results")
    public String searchUsersByHashtags(@RequestParam("hashtags") String hashtagsParam, Model model) {
        List<String> hashtags = Arrays.asList(hashtagsParam.split(","));
        System.out.println(hashtags);

        List<String> users = hashtagService.findUsersByHashtags(hashtags);
        System.out.println(users);
        model.addAttribute("users", users);
        model.addAttribute("hashtags", hashtags);
        return "search-results";
    }
}
