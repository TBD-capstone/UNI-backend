package uni.backend.service;


import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uni.backend.domain.Ad;
import uni.backend.domain.Profile;
import uni.backend.domain.dto.AdListResponse;
import uni.backend.domain.dto.AdRequest;
import uni.backend.domain.dto.AdResponse;
import uni.backend.enums.AdStatus;
import uni.backend.repository.AdRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdService {

    @Autowired
    private final AdRepository adRepository;
    private final AwsS3Service awsS3Service;

//    public Ad uploadAd(AdRequest adRequest) {
//        Ad ad = Ad.builder()
//            .advertiser(adRequest.getAdvertiser())
//            .title(adRequest.getTitle())
//            .adStatus(
//                Objects.equals(adRequest.getAdStatus(), "posted") ? AdStatus.POSTED
//                    : AdStatus.ENDED)
//            .startDate(adRequest.getStartDate())
//            .endDate(adRequest.getEndDate())
//            .imageUrl(adRequest.getImageUrl())
//            .build();
//        return adRepository.save(ad);
//    }

    public Ad findAdById(Integer id) {
        return adRepository.findById(id).orElse(null);
    }

    public Ad updateAdStatus(Integer id, String status) {
        Ad ad = findAdById(id);
        ad.setAdStatus(status == "posted" ? AdStatus.POSTED : AdStatus.ENDED);
        return adRepository.save(ad);
    }

    public void deleteAdById(Integer id) {
        Ad ad = findAdById(id);
        adRepository.delete(ad);
        return;
    }

    public AdListResponse findAll() {
        List<AdResponse> adResponses = adRepository.findAll().stream()
            .map(ad -> {
                AdResponse adResponse = new AdResponse();
                adResponse.setAdId(ad.getAdId());
                adResponse.setAdvertiser(ad.getAdvertiser());
                adResponse.setTitle(ad.getTitle());
                adResponse.setAdStatus(ad.getAdStatus() == AdStatus.POSTED ? "posted" : "ended");
                adResponse.setStartDate(ad.getStartDate());
                adResponse.setEndDate(ad.getEndDate());
                adResponse.setImageUrl(ad.getImageUrl());
                return adResponse;
            })
            .collect(Collectors.toList());

        AdListResponse adListResponse = new AdListResponse();
        adListResponse.setAds(adResponses);
        return adListResponse;
    }

    public Ad uploadAd(Integer userId, MultipartFile adImg, AdRequest adRequest) {
        if (adImg.isEmpty() || Objects.isNull(adImg.getOriginalFilename())) {
            throw new IllegalArgumentException("이미지가 비어 있거나 잘못된 파일입니다.");
        }

        String imageUrl = awsS3Service.upload(adImg, "ads", userId);

        Ad ad = Ad.builder()
            .advertiser(adRequest.getAdvertiser())
            .title(adRequest.getTitle())
            .adStatus(
                Objects.equals(adRequest.getAdStatus(), "posted") ? AdStatus.POSTED : AdStatus.ENDED
            )
            .startDate(adRequest.getStartDate())
            .endDate(adRequest.getEndDate())
            .imageUrl(imageUrl)
            .build();

        return adRepository.save(ad);
    }
}
