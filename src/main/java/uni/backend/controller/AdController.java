package uni.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uni.backend.domain.Ad;
import uni.backend.domain.dto.AdListResponse;
import uni.backend.domain.dto.AdRequest;
import uni.backend.domain.dto.UpdateAdStatusRequest;
import uni.backend.enums.AdStatus;
import uni.backend.service.AdService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdController {

    private final AdService adService;

    // 모든 광고 조회
    @GetMapping("/admin/ad")
    public ResponseEntity<AdListResponse> getAllAds() {
        AdListResponse adListResponse = adService.findAll();
        return ResponseEntity.ok(adListResponse);
    }

    // 새로운 광고 생성
    @PostMapping("/admin/ad/new")
    public ResponseEntity<Ad> createAd(
        @RequestPart("adImg") MultipartFile adImg,
        @RequestPart("adRequest") AdRequest adRequest) {

        Ad createdAd = adService.uploadAd(adImg, adRequest);
        return ResponseEntity.ok(createdAd);
    }


    // 광고 상태 수정
    @PostMapping("/admin/ad/update-status")
    public ResponseEntity<Void> updateAdStatus(@RequestBody @Valid UpdateAdStatusRequest request) {
        adService.updateAdStatus(request.getAdId(), request.getNewStatus());
        return ResponseEntity.ok().build();
    }


    // 랜덤 ACTIVE 광고 반환
    @GetMapping("/ad")
    public ResponseEntity<Ad> getRandomActiveAd() {
        Ad activeAd = adService.getRandomActiveAd();
        return ResponseEntity.ok(activeAd);
    }
}
