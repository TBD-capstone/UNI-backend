package uni.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import uni.backend.domain.Ad;
import uni.backend.domain.dto.AdListResponse;
import uni.backend.domain.dto.AdRequest;
import uni.backend.enums.AdStatus;
import uni.backend.repository.AdRepository;

class AdServiceTest {

    private static final Logger log = LogManager.getLogger(AdServiceTest.class);

    @InjectMocks
    private AdService adService;

    @Mock
    private AwsS3Service awsS3Service;

    @Mock
    private AdRepository adRepository;

    private Ad ad1, ad2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ad1 = Ad.builder()
            .adId(1)
            .advertiser("test1")
            .title("starbucks")
            .adStatus(AdStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.of(2025, 1, 1))
            .imageUrl("image1 url")
            .build();

        ad2 = Ad.builder()
            .adId(2)
            .advertiser("test2")
            .title("macbook")
            .adStatus(AdStatus.ENDED)
            .startDate(LocalDate.of(2024, 9, 1))
            .endDate(LocalDate.now())
            .imageUrl("image2 url")
            .build();

        when(adRepository.findById(1)).thenReturn(Optional.of(ad1));
        when(adRepository.findById(2)).thenReturn(Optional.of(ad2));
        when(adRepository.save(any(Ad.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("광고 업로드 성공 테스트")
    void 광고_업로드() {
        // given
        MockMultipartFile mockFile = new MockMultipartFile(
            "adImg",
            "test.jpg",
            "image/jpeg",
            new byte[]{1, 2, 3}
        );

        AdRequest adRequest = AdRequest.builder()
            .advertiser("test3")
            .title("samsung")
            .adStatus(AdStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.of(2026, 1, 1))
            .build();

        String mockUrl = "https://tbd-bucket.s3.ap-northeast-2.amazonaws.com/ads/test.jpg";

        // S3 업로드 Mock 설정
        when(awsS3Service.uploadAdImage(mockFile)).thenReturn(mockUrl);

        // when
        Ad ad = adService.uploadAd(mockFile, adRequest);

        // then
        assertNotNull(ad);
        assertEquals("test3", ad.getAdvertiser());
        assertEquals("samsung", ad.getTitle());
        assertEquals(AdStatus.ACTIVE, ad.getAdStatus());
        assertEquals(mockUrl, ad.getImageUrl());
    }

    @Test
    @DisplayName("광고 상태 변경 성공 테스트")
    void 광고_상태_변경() {
        // given
        Integer adId = ad1.getAdId();

        // when
        adService.updateAdStatus(adId, AdStatus.ENDED);

        // then
        verify(adRepository).save(ad1);
        assertEquals(AdStatus.ENDED, ad1.getAdStatus());
    }

    @Test
    @DisplayName("전체 광고 로드 성공 테스트")
    void 전체_광고_로드() {
        // given
        when(adRepository.findAll()).thenReturn(List.of(ad1, ad2));

        // when
        AdListResponse allAds = adService.findAll();

        // then
        assertNotNull(allAds);
        assertEquals(2, allAds.getAds().size());
    }

    @Test
    @DisplayName("광고 삭제 성공 테스트")
    void 광고_삭제() {
        // given
        Integer adId = ad2.getAdId();

        // when
        adService.deleteAdById(adId);

        // then
        verify(adRepository).delete(ad2);
    }

    @Test
    @DisplayName("랜덤 ACTIVE 광고 반환 테스트")
    void 랜덤_ACTIVE_광고_반환() {
        // given
        when(adRepository.findAll()).thenReturn(List.of(ad1));

        // when
        Ad activeAd = adService.getRandomActiveAd();

        // then
        assertNotNull(activeAd);
        assertEquals(AdStatus.ACTIVE, activeAd.getAdStatus());
    }

    @Test
    @DisplayName("랜덤 ACTIVE 광고가 없을 때 예외 발생")
    void 랜덤_ACTIVE_광고_없음_예외발생() {
        // given
        when(adRepository.findAll()).thenReturn(List.of()); // 비어 있는 리스트 반환

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> adService.getRandomActiveAd());
        assertEquals("ACTIVE 상태의 광고가 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("광고 업로드 실패 - 이미지 파일 없음")
    void 광고_업로드_이미지없음() {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile("adImg", "", "image/jpeg",
            new byte[]{});

        AdRequest adRequest = AdRequest.builder()
            .advertiser("test3")
            .title("samsung")
            .adStatus(AdStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.of(2026, 1, 1))
            .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> adService.uploadAd(emptyFile, adRequest));
        assertEquals("이미지가 비어 있거나 잘못된 파일입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("광고 상태 변경 실패 - ID 없음")
    void 광고_상태_변경_ID없음() {
        // given
        Integer invalidAdId = 999;

        when(adRepository.findById(invalidAdId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> adService.updateAdStatus(invalidAdId, AdStatus.ACTIVE));
        assertEquals("해당 ID의 광고가 존재하지 않습니다: " + invalidAdId, exception.getMessage());
    }

    @Test
    @DisplayName("광고 업로드 실패 - AWS S3 업로드 실패")
    void 광고_업로드_AWSS3_업로드_실패() {
        // given
        MockMultipartFile mockFile = new MockMultipartFile(
            "adImg",
            "test.jpg",
            "image/jpeg",
            new byte[]{1, 2, 3}
        );

        AdRequest adRequest = AdRequest.builder()
            .advertiser("test3")
            .title("samsung")
            .adStatus(AdStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.of(2026, 1, 1))
            .build();

        when(awsS3Service.uploadAdImage(mockFile)).thenThrow(new RuntimeException("S3 업로드 실패"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> adService.uploadAd(mockFile, adRequest));
        assertEquals("S3 업로드 실패", exception.getMessage());
    }

}
