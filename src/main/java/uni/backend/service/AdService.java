package uni.backend.service;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uni.backend.domain.Ad;
import uni.backend.domain.dto.AdListResponse;
import uni.backend.domain.dto.AdRequest;
import uni.backend.domain.dto.AdResponse;
import uni.backend.enums.AdStatus;
import uni.backend.repository.AdRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdService {

    private final AdRepository adRepository;
    private final AwsS3Service awsS3Service;

    // 특정 광고 조회
    public Ad findAdById(Integer id) {
        return adRepository.findById(id).orElseThrow(() ->
            new IllegalArgumentException("해당 ID의 광고가 존재하지 않습니다: " + id));
    }

    // 광고 삭제
    @Transactional
    public void deleteAdById(Integer id) {
        Ad ad = findAdById(id);
        adRepository.delete(ad);
    }

    // 모든 광고 조회
    public AdListResponse findAll() {
        List<AdResponse> adResponses = adRepository.findAll().stream()
            .map(ad -> AdResponse.builder()
                .adId(ad.getAdId())
                .advertiser(ad.getAdvertiser())
                .title(ad.getTitle())
                .adStatus(ad.getAdStatus().name().toLowerCase())
                .startDate(ad.getStartDate())
                .endDate(ad.getEndDate())
                .imageUrl(ad.getImageUrl())
                .build())
            .collect(Collectors.toList());

        return AdListResponse.builder()
            .ads(adResponses)
            .build();
    }

    // 광고 업로드
    public Ad uploadAd(MultipartFile adImg, AdRequest adRequest) {
        if (adImg.isEmpty() || Objects.isNull(adImg.getOriginalFilename())) {
            throw new IllegalArgumentException("이미지가 비어 있거나 잘못된 파일입니다.");
        }

        // 광고 이미지를 S3에 업로드
        String imageUrl = awsS3Service.uploadAdImage(adImg);

        // 광고 객체 생성 및 저장
        Ad ad = Ad.builder()
            .advertiser(adRequest.getAdvertiser())
            .title(adRequest.getTitle())
            .adStatus(adRequest.getAdStatus())
            .startDate(adRequest.getStartDate())
            .endDate(adRequest.getEndDate())
            .imageUrl(imageUrl)
            .build();

        return adRepository.save(ad);
    }


    // 광고 상태 업데이트
    @Transactional
    public void updateAdStatus(Integer adId, AdStatus status) {
        Ad ad = findAdById(adId);

        log.info("광고 ID {}의 상태를 {}로 변경합니다.", adId, status);
        ad.setAdStatus(status);
        adRepository.save(ad);
    }

    // 랜덤 ACTIVE 광고 반환
    public Ad getRandomActiveAd() {
        List<Ad> activeAds = adRepository.findAll().stream()
            .filter(ad -> ad.getAdStatus() == AdStatus.ACTIVE)
            .collect(Collectors.toList());

        if (activeAds.isEmpty()) {
            throw new IllegalStateException("ACTIVE 상태의 광고가 없습니다.");
        }

        Random random = new Random();
        return activeAds.get(random.nextInt(activeAds.size()));
    }
}
