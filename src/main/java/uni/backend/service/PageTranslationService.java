package uni.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import uni.backend.domain.Hashtag;
import uni.backend.domain.University;
import uni.backend.domain.dto.HomeProfileResponse;
import uni.backend.domain.dto.IndividualProfileResponse;
import uni.backend.domain.dto.IndividualTranslationResponse;
import uni.backend.domain.dto.MarkerResponse;
import uni.backend.domain.dto.QnaResponse;
import uni.backend.domain.dto.ReplyResponse;
import uni.backend.domain.dto.ReviewReplyResponse;
import uni.backend.domain.dto.ReviewResponse;
import uni.backend.domain.dto.TranslationRequest;
import uni.backend.domain.dto.TranslationResponse;
import uni.backend.repository.HashtagRepository;
import uni.backend.repository.UniversityRepository;
import uni.backend.util.MainCategoryMap;

@Service
public class PageTranslationService {

    private static final Logger log = LoggerFactory.getLogger(PageTranslationService.class);
    @Autowired
    private TranslationService translationService;

    @Autowired
    private UniversityRepository universityRepository;

    private List<String> translateHashtag(List<String> hashtags, String acceptLanguage) {
        if (hashtags == null || hashtags.isEmpty()) {
            return hashtags; // 입력값이 비어있으면 그대로 반환
        }
        acceptLanguage = translationService.determineTargetLanguage(acceptLanguage);
        List<String> translatedHashtags = new ArrayList<>();

        for (String hashtag : hashtags) {
            String koreanKeyword = mapToKoreanKeyword(hashtag);

            Map<String, String> translations = MainCategoryMap.KOREAN_HASHTAG_MAP.get(
                koreanKeyword);
            if (translations == null) {
                translatedHashtags.add(hashtag); // 번역하지 않고 원본 추가
                continue;
            }

            String translated = translations.getOrDefault(acceptLanguage.toLowerCase(),
                koreanKeyword);
            translatedHashtags.add(translated);
        }

        return translatedHashtags;
    }

    private String getUnivNameByLanguage(String univName, String acceptLanguage) {
        Optional<University> universityOpt = universityRepository.findByUniName(univName);
        University university = universityOpt.orElse(null);
        String translatedUnivName;
        if (university != null) {
            translatedUnivName = switch (acceptLanguage) {
                case "zh" -> university.getZhUniName();
                case "en" -> university.getEnUniName();
                default -> university.getUniName();
            };
        } else {
            translatedUnivName = univName;
        }
        return translatedUnivName;
    }

    private String translateOneText(String text, String acceptLanguage) {
        List<String> oneList = new ArrayList<>(List.of());
        oneList.add(text);
        TranslationRequest translationRequest = new TranslationRequest();
        translationRequest.setText(oneList);
        translationRequest.setSource_lang("ko");
        translationRequest.setTarget_lang(acceptLanguage);
        TranslationResponse translationResponse = translationService.translate(translationRequest);
        return translationResponse.getTranslations().getFirst().getText();
    }

    public void translateProfileResponse(IndividualProfileResponse individualProfileResponse,
        String acceptLanguage) {
        if (individualProfileResponse == null || acceptLanguage == null) {
            return;
        }
        acceptLanguage = translationService.determineTargetLanguage(acceptLanguage);

        individualProfileResponse.setDescription(translateOneText(
            individualProfileResponse.getDescription(), acceptLanguage));
        individualProfileResponse.setUniv(
            getUnivNameByLanguage(individualProfileResponse.getUniv(), acceptLanguage));
        List<String> hashtags = individualProfileResponse.getHashtags();
        translateHashtag(hashtags, acceptLanguage);

        String region = individualProfileResponse.getRegion();
        if (region != null && !region.isEmpty()) {
            String translatedRegion = translateOneText(region, acceptLanguage);
            individualProfileResponse.setRegion(translatedRegion);
        }
    }

    public void translateQna(List<QnaResponse> response, String acceptLanguage) {
        acceptLanguage = translationService.determineTargetLanguage(acceptLanguage);
        TranslationRequest translationRequest = new TranslationRequest();
        translationRequest.setTarget_lang(acceptLanguage);

        for (QnaResponse individualQnaResponse : response) {
            List<String> text = new ArrayList<>(List.of());
            text.add(individualQnaResponse.getContent());
            for (ReplyResponse individualReplyResponse : individualQnaResponse.getReplies()) {
                text.add(individualReplyResponse.getContent());
            }
            translationRequest.setText(text);
            TranslationResponse translationResponse = translationService.translate(
                translationRequest);
            List<IndividualTranslationResponse> translations = translationResponse.getTranslations();
            individualQnaResponse.setContent(translations.getFirst().getText());

            int i = 1;
            for (ReplyResponse individualReplyResponse : individualQnaResponse.getReplies()) {
                individualReplyResponse.setContent(translations.get(i++).getText());
            }
        }
    }

    public void translateReview(List<ReviewResponse> response, String acceptLanguage) {
        acceptLanguage = translationService.determineTargetLanguage(acceptLanguage);
        TranslationRequest translationRequest = new TranslationRequest();
        translationRequest.setTarget_lang(acceptLanguage);

        for (ReviewResponse individualReviewResponse : response) {
            List<String> text = new ArrayList<>(List.of());
            text.add(individualReviewResponse.getContent());
            for (ReviewReplyResponse individualReplyResponse : individualReviewResponse.getReplies()) {
                text.add(individualReplyResponse.getContent());
            }
            translationRequest.setText(text);
            TranslationResponse translationResponse = translationService.translate(
                translationRequest);
            List<IndividualTranslationResponse> translations = translationResponse.getTranslations();
            individualReviewResponse.setContent(translations.getFirst().getText());

            int i = 1;
            for (ReviewReplyResponse individualReplyResponse : individualReviewResponse.getReplies()) {
                individualReplyResponse.setContent(translations.get(i++).getText());
            }
        }

    }

    public void translateHomeResponse(Page<HomeProfileResponse> results, String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isEmpty()) {
            return;
        }
        acceptLanguage = translationService.determineTargetLanguage(acceptLanguage);

        for (HomeProfileResponse profile : results.getContent()) {
            List<String> data = translateHashtag(profile.getHashtags(), acceptLanguage);
            // 첫번째껄 대학이름으로?
            profile.setUnivName(data.getFirst());
            if (data.size() > 1) {
                profile.setHashtags(data.subList(1, data.size()));
            }
        }

    }

    public void translateMarkers(List<MarkerResponse> markers, String acceptLanguage) {
        acceptLanguage = translationService.determineTargetLanguage(acceptLanguage);
        TranslationRequest translationRequest = new TranslationRequest();
        translationRequest.setSource_lang("ko");
        translationRequest.setTarget_lang(acceptLanguage);
        List<String> text = new ArrayList<>(List.of());

        for (MarkerResponse individualMarkerResponse : markers) {
            text.add(individualMarkerResponse.getName());
            text.add(individualMarkerResponse.getDescription());
        }
        translationRequest.setText(text);

        // 번역 요청 수행
        TranslationResponse translationResponse = translationService.translate(translationRequest);

        // 번역된 텍스트를 markers에 다시 설정
        List<IndividualTranslationResponse> translations = translationResponse.getTranslations();
        int index = 0; // 번역 텍스트의 인덱스를 추적
        for (MarkerResponse marker : markers) {
            if (index < translations.size()) {
                marker.setName(translations.get(index++).getText()); // 이름 설정
            }
            if (index < translations.size()) {
                marker.setDescription(translations.get(index++).getText()); // 설명 설정
            }
        }

    }

    public String mapToKoreanKeyword(String userInput) {
        return MainCategoryMap.HASHTAG_TRANSLATION_MAP.getOrDefault(userInput.toLowerCase(),
            userInput);
    }

    public String translateToLanguage(String koreanKeyword, String targetLanguage) {
        Map<String, String> translations = MainCategoryMap.KOREAN_HASHTAG_MAP.get(koreanKeyword);
        if (translations != null) {
            return translations.getOrDefault(targetLanguage.toLowerCase(), koreanKeyword);
        }
        // 맵에 없는 경우 원래 키워드 반환
        return koreanKeyword;
    }


}
