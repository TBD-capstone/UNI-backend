package uni.backend.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.util.StringUtils.capitalize;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

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
import uni.backend.repository.UniversityRepository;
import uni.backend.util.MainCategoryMap;

class PageTranslationServiceTest {

    @Mock
    private TranslationService translationService;

    @Mock
    private UniversityRepository universityRepository;

    @InjectMocks
    private PageTranslationService pageTranslationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testTranslateHomeResponse() {
        // Given: Mock 데이터 생성
        List<HomeProfileResponse> mockProfiles = new ArrayList<>();
        HomeProfileResponse mockProfile = new HomeProfileResponse();
        mockProfile.setUnivName("서울대학교");
        mockProfile.setHashtags(List.of("여행"));
        mockProfiles.add(mockProfile);

        Page<HomeProfileResponse> profiles = new PageImpl<>(mockProfiles);

        University mockUniversity = new University();
        mockUniversity.setUniName("서울대학교");
        mockUniversity.setEnUniName("Seoul National University");
        mockUniversity.setZhUniName("首尔大学");

        // When: Mock 동작 정의
        when(universityRepository.findByUniName("서울대학교")).thenReturn(Optional.of(mockUniversity));
        when(translationService.determineTargetLanguage("en")).thenReturn("en");

        // Act: translateHomeResponse 호출
        pageTranslationService.translateHomeResponse(profiles, "en");

        // Then: 번역 결과 검증
        assertEquals("Seoul National University", profiles.getContent().get(0).getUnivName());
        assertEquals(List.of("Trip"), profiles.getContent().get(0).getHashtags());
    }

    @Test
    void testTranslateHomeResponse_NoTranslationForHashtags() {
        // Given: Mock 데이터 생성
        List<HomeProfileResponse> mockProfiles = new ArrayList<>();
        HomeProfileResponse mockProfile = new HomeProfileResponse();
        mockProfile.setUnivName("Unknown University");
        mockProfile.setHashtags(List.of("unknownTag"));
        mockProfiles.add(mockProfile);

        Page<HomeProfileResponse> profiles = new PageImpl<>(mockProfiles);

        // When: Mock 동작 정의
        when(universityRepository.findByUniName("Unknown University")).thenReturn(Optional.empty());
        when(translationService.determineTargetLanguage("en")).thenReturn("en");

        // Act: translateHomeResponse 호출
        pageTranslationService.translateHomeResponse(profiles, "en");

        // Then: 번역 결과 검증
        assertEquals("Unknown University", profiles.getContent().get(0).getUnivName());
        assertEquals(List.of("unknownTag"), profiles.getContent().get(0).getHashtags());
    }

    @Test
    void testTranslateProfileResponse() {
        // Given: Mock 데이터 생성
        IndividualProfileResponse mockProfile = new IndividualProfileResponse();
        mockProfile.setDescription("안녕하세요");
        mockProfile.setRegion("서울");
        mockProfile.setHashtags(List.of("해시태그"));
        mockProfile.setUniv("서울대학교");

        when(translationService.determineTargetLanguage("en")).thenReturn("en");
        when(translationService.translate(any(TranslationRequest.class)))
            .thenAnswer(invocation -> {
                TranslationRequest request = invocation.getArgument(0);
                List<IndividualTranslationResponse> translations = new ArrayList<>();
                for (String text : request.getText()) {
                    IndividualTranslationResponse response = new IndividualTranslationResponse();
                    response.setText("Translated: " + text);
                    translations.add(response);
                }
                TranslationResponse response = new TranslationResponse();
                response.setTranslations(translations);
                return response;
            });

        // Act
        pageTranslationService.translateProfileResponse(mockProfile, "en");

        // Then
        assertEquals("Translated: 안녕하세요", mockProfile.getDescription());
        assertEquals("Translated: 서울", mockProfile.getRegion());
        assertEquals(List.of("해시태그"), mockProfile.getHashtags());
        assertEquals("서울대학교", mockProfile.getUniv());
    }

    @Test
    void testTranslateMarkers() {
        // Given: Mock 데이터 생성
        MarkerResponse mockMarker = new MarkerResponse();
        mockMarker.setName("마커 이름");
        mockMarker.setDescription("마커 설명");

        when(translationService.determineTargetLanguage("en")).thenReturn("en");
        when(translationService.translate(any(TranslationRequest.class)))
            .thenAnswer(invocation -> {
                TranslationRequest request = invocation.getArgument(0);
                List<IndividualTranslationResponse> translations = new ArrayList<>();
                for (String text : request.getText()) {
                    IndividualTranslationResponse response = new IndividualTranslationResponse();
                    response.setText("Translated: " + text);
                    translations.add(response);
                }
                TranslationResponse response = new TranslationResponse();
                response.setTranslations(translations);
                return response;
            });

        // Act
        pageTranslationService.translateMarkers(List.of(mockMarker), "en");

        // Then
        assertEquals("Translated: 마커 이름", mockMarker.getName());
        assertEquals("Translated: 마커 설명", mockMarker.getDescription());
    }

    @Test
    void testTranslateQna() {
        // Given
        QnaResponse mockQna = new QnaResponse();
        mockQna.setContent("질문 내용");
        ReplyResponse mockReply = new ReplyResponse();
        mockReply.setContent("답변 내용");
        mockQna.setReplies(List.of(mockReply));

        when(translationService.determineTargetLanguage("en")).thenReturn("en");
        when(translationService.translate(any(TranslationRequest.class)))
            .thenAnswer(invocation -> {
                TranslationRequest request = invocation.getArgument(0);
                List<IndividualTranslationResponse> translations = new ArrayList<>();
                for (String text : request.getText()) {
                    IndividualTranslationResponse response = new IndividualTranslationResponse();
                    response.setText("Translated: " + text);
                    translations.add(response);
                }
                return new TranslationResponse(translations);
            });

        // When
        pageTranslationService.translateQna(List.of(mockQna), "en");

        // Then
        assertEquals("Translated: 질문 내용", mockQna.getContent());
        assertEquals("Translated: 답변 내용", mockQna.getReplies().get(0).getContent());
    }

    @Test
    void testTranslateQna_EmptyReplies() {
        // Given
        QnaResponse mockQna = new QnaResponse();
        mockQna.setContent("질문 내용");
        mockQna.setReplies(new ArrayList<>()); // 빈 리스트

        when(translationService.determineTargetLanguage("en")).thenReturn("en");
        when(translationService.translate(any(TranslationRequest.class)))
            .thenAnswer(invocation -> {
                TranslationRequest request = invocation.getArgument(0);
                return new TranslationResponse(
                    request.getText().stream()
                        .map(text -> new IndividualTranslationResponse("ko", "Translated: " + text))
                        .toList()
                );
            });

        // When
        pageTranslationService.translateQna(List.of(mockQna), "en");

        // Then
        assertEquals("Translated: 질문 내용", mockQna.getContent());
        assertTrue(mockQna.getReplies().isEmpty());
    }

    @Test
    void testTranslateReview() {
        // Given
        ReviewResponse mockReview = new ReviewResponse();
        mockReview.setContent("리뷰 내용");
        ReviewReplyResponse mockReply = new ReviewReplyResponse();
        mockReply.setContent("리뷰 답변");
        mockReview.setReplies(List.of(mockReply));

        when(translationService.determineTargetLanguage("en")).thenReturn("en");
        when(translationService.translate(any(TranslationRequest.class)))
            .thenAnswer(invocation -> {
                TranslationRequest request = invocation.getArgument(0);
                return new TranslationResponse(
                    request.getText().stream()
                        .map(text -> new IndividualTranslationResponse("ko", "Translated: " + text))
                        .toList()
                );
            });

        // When
        pageTranslationService.translateReview(List.of(mockReview), "en");

        // Then
        assertEquals("Translated: 리뷰 내용", mockReview.getContent());
        assertEquals("Translated: 리뷰 답변", mockReview.getReplies().get(0).getContent());
    }

    @Test
    void testTranslateReview_NoReplies() {
        // Given
        ReviewResponse mockReview = new ReviewResponse();
        mockReview.setContent("리뷰 내용");
        mockReview.setReplies(new ArrayList<>()); // 빈 대댓글 리스트

        when(translationService.determineTargetLanguage("en")).thenReturn("en");
        when(translationService.translate(any(TranslationRequest.class)))
            .thenAnswer(invocation -> {
                TranslationRequest request = invocation.getArgument(0);
                return new TranslationResponse(
                    request.getText().stream()
                        .map(text -> new IndividualTranslationResponse("ko", "Translated: " + text))
                        .toList()
                );
            });

        // When
        pageTranslationService.translateReview(List.of(mockReview), "en");

        // Then
        assertEquals("Translated: 리뷰 내용", mockReview.getContent());
        assertTrue(mockReview.getReplies().isEmpty());
    }


}
