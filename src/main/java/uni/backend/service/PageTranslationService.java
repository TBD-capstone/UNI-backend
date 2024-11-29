package uni.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import uni.backend.domain.dto.HomeDataResponse;
import uni.backend.domain.dto.HomeProfileResponse;
import uni.backend.domain.dto.IndividualProfileResponse;
import uni.backend.domain.dto.IndividualTranslationResponse;
import uni.backend.domain.dto.QnaResponse;
import uni.backend.domain.dto.ReplyResponse;
import uni.backend.domain.dto.ReviewReplyResponse;
import uni.backend.domain.dto.ReviewResponse;
import uni.backend.domain.dto.TranslationRequest;
import uni.backend.domain.dto.TranslationResponse;

@Service
public class PageTranslationService {

    @Autowired
    private TranslationService translationService;

    private List<String> extractUnivHashtag(String univName, List<String> hashtags,
        String acceptLanguage, String content) {
        TranslationRequest translationRequest = new TranslationRequest();
        List<String> newList = new ArrayList<>(hashtags);
        if (content != null) {
            newList.addFirst(content);
        }
        newList.addFirst(univName);
        translationRequest.setText(newList);
        translationRequest.setSource_lang("ko");
        translationRequest.setTarget_lang(acceptLanguage);
        TranslationResponse translationResponse = translationService.translate(
            translationRequest);
        return translationResponse.getTranslations()
            .stream()
            .map(IndividualTranslationResponse::getText)
            .collect(Collectors.toList());
    }

    public void translateProfileResponse(IndividualProfileResponse individualProfileResponse,
        String acceptLanguage) {
        if (individualProfileResponse == null || acceptLanguage == null) {
            return;
        }
        acceptLanguage = translationService.determineTargetLanguage(acceptLanguage);
        List<String> data = extractUnivHashtag(individualProfileResponse.getUniv(),
            individualProfileResponse.getHashtags(), acceptLanguage,
            individualProfileResponse.getDescription());
        individualProfileResponse.setUniv(data.getFirst());
        if (data.size() > 1) {
            individualProfileResponse.setDescription(data.get(1));
        }
        if (data.size() > 2) {
            individualProfileResponse.setHashtags(data.subList(2, data.size()));
        }
    }

    public void translateHomeDataResponse(HomeDataResponse homeDataResponse,
        String acceptLanguage) {
        if (homeDataResponse == null || homeDataResponse.getData() == null) {
            return;
        }

        for (HomeProfileResponse profile : homeDataResponse.getData()) {
            List<String> data = extractUnivHashtag(profile.getUnivName(), profile.getHashtags(),
                acceptLanguage, null);
            profile.setUnivName(data.getFirst());
            if (data.size() > 1) {
                profile.setHashtags(data.subList(1, data.size()));
            }
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
            List<String> data = extractUnivHashtag(profile.getUnivName(), profile.getHashtags(),
                acceptLanguage, null);
            profile.setUnivName(data.getFirst());
            if (data.size() > 1) {
                profile.setHashtags(data.subList(1, data.size()));
            }
        }

    }
}
