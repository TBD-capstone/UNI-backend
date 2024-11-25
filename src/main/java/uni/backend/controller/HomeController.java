package uni.backend.controller;

import java.awt.print.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uni.backend.domain.dto.HomeDataResponse;
import uni.backend.domain.dto.HomeProfileResponse;
import uni.backend.service.HomeService;
import uni.backend.service.ProfileService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // 프론트엔드 도메인 허용
public class HomeController {

    @Autowired
    HomeService homeService;


    @GetMapping("/home")
    public Page<HomeDataResponse> searchByUnivNameAndHashtags(
        @RequestParam(required = false) String univName,
        @RequestParam(required = false) List<String> hashtags,
        @RequestParam(defaultValue = "1") int page) {
        return homeService.searchByUnivNameAndHashtags(univName, hashtags, page - 1);
    }
}
