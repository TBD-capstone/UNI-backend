package uni.backend.controller;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uni.backend.domain.Ad;
import uni.backend.domain.dto.AdEditRequest;
import uni.backend.domain.dto.AdListResponse;
import uni.backend.domain.dto.AdRequest;
import uni.backend.enums.AdStatus;
import uni.backend.service.AdService;

@RestController
@RequestMapping("/api/admin/ad")
@RequiredArgsConstructor
public class AdController {

    @Autowired
    private AdService adService;

    private AdStatus extractStatus(String status) {
        return AdStatus.POSTED;
    }

    @GetMapping()
    public ResponseEntity<AdListResponse> getAllAds() {
        AdListResponse adListResponse = adService.findAll();
        return ResponseEntity.ok(adListResponse);
    }

    @PostMapping("/new")
    public Ad createAd(
        @RequestParam("userId") Integer userId,
        @RequestParam("adImg") MultipartFile adImg,
        @RequestBody AdRequest adRequest) {

        return adService.uploadAd(userId, adImg, adRequest);
    }

    @PostMapping()
    public Ad updateAd(@RequestBody AdEditRequest adEditRequest) {
        return adService.updateAdStatus(adEditRequest.getAdId(), adEditRequest.getAdStatus());
    }
}
