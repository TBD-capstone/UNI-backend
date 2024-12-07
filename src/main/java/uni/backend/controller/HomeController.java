package uni.backend.controller;

import java.awt.print.Pageable;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uni.backend.domain.dto.HomeDataResponse;
import uni.backend.domain.dto.HomeProfileResponse;
import uni.backend.service.HomeService;
import uni.backend.service.PageTranslationService;
import uni.backend.service.ProfileService;

import java.util.List;
import uni.backend.service.TranslationService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // 프론트엔드 도메인 허용
public class HomeController {

    @Autowired
    HomeService homeService;

    @Autowired
    TranslationService translationService;

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
