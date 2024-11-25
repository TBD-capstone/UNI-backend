package uni.backend.controller;

import java.awt.print.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<HomeDataResponse> getHomeProfiles(
        @PageableDefault(size = 8) Pageable pageable) {

        HomeDataResponse homeDataResponse = homeService.getHomeDataProfiles(pageable);

        return ResponseEntity.ok(homeDataResponse);
    }
}
