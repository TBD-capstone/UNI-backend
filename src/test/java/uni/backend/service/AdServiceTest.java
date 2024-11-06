package uni.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Ad;
import uni.backend.domain.dto.AdListResponse;
import uni.backend.domain.dto.AdRequest;
import uni.backend.enums.AdStatus;
import uni.backend.repository.AdRepository;

@SpringBootTest
@Transactional
class AdServiceTest {

    private static final Logger log = LogManager.getLogger(AdServiceTest.class);
    @Autowired
    private AdService adService;

    @Autowired
    private AdRepository adRepository;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        Ad ad1 = Ad.builder()
            .advertiser("test1")
            .title("starbucks")
            .adStatus(AdStatus.POSTED)
            .startDate(LocalDate.now())
            .startDate(LocalDate.of(2025, 1, 1))
            .imageUrl("image url")
            .build();

        Ad ad2 = Ad.builder()
            .advertiser("test2")
            .title("macbook")
            .adStatus(AdStatus.ENDED)
            .startDate(LocalDate.of(2024, 9, 1))
            .endDate(LocalDate.now())
            .imageUrl("image2 url")
            .build();

        adRepository.save(ad1);
        adRepository.save(ad2);
    }

//    @AfterEach
//    @Transactional
//    void tearDown() throws Exception {
//        Integer test1 = adRepository.findByAdvertiser("test1");
//        System.out.println(test1);
//        adRepository.deleteById(adRepository.findByAdvertiser("test1"));
//        adRepository.deleteById(adRepository.findByAdvertiser("test2"));
//    }

    @Test
    void 광고_업로드() {
        AdRequest adRequest = AdRequest.builder()
            .advertiser("test3")
            .title("samsung")
            .adStatus("posted")
            .startDate(LocalDate.now())
            .endDate(LocalDate.of(2026, 1, 1))
            .imageUrl("image3 url")
            .build();

        Ad ad = adService.uploadAd(adRequest);

        assertEquals(ad, adRepository.findById(ad.getAdId()).get());
    }


    @Test
    void 광고_상태_변경() {
        Ad ad = adRepository.findByAdvertiser("test1").orElse(null);
        assert ad != null;
        Integer test1Id = ad.getAdId();
        System.out.println("test1Id: " + test1Id);
        log.info("test1Id: " + test1Id);
        adService.updateAdStatus(test1Id, "ended");
        assertEquals(AdStatus.ENDED, adRepository.findById(test1Id).get().getAdStatus());
    }

    @Test
    void 전체_광고_로드() {
        AdListResponse allAds = adService.findAll();
        Assertions.assertTrue(allAds.getAds().size() > 0);
    }
}