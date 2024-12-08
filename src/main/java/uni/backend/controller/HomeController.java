package uni.backend.controller;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uni.backend.domain.dto.HomeProfileResponse;
import uni.backend.service.HomeService;
import uni.backend.service.PageTranslationService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HomeController {

    @Autowired
    HomeService homeService;

    @Autowired
    PageTranslationService pageTranslationService;

    @GetMapping("/home")
    public ResponseEntity<Page<HomeProfileResponse>> searchByUnivNameAndHashtags(
        @RequestParam(required = false) String univName,
        @RequestParam(required = false) List<String> hashtags,
        @RequestParam int page,
        @RequestParam(defaultValue = "newest") String sort,
        @RequestHeader(name = "Accept-Language", required = false) String acceptLanguage) {

        univName = (univName != null) ? univName.trim() : null;
        if (hashtags != null) {
            hashtags = hashtags.stream().map(String::trim).collect(Collectors.toList());
            homeService.changeHashtagsToKorean(hashtags);
        }

        Page<HomeProfileResponse> results = homeService.searchByUnivNameAndHashtags(univName,
            hashtags, page, sort);
        pageTranslationService.translateHomeResponse(results, acceptLanguage);
        return ResponseEntity.ok(results);
    }
}
