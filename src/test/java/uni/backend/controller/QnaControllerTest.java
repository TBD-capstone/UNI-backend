package uni.backend.controller;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import uni.backend.domain.Qna;
import uni.backend.domain.User;
import uni.backend.domain.dto.QnaCreateRequest;
import uni.backend.domain.dto.QnaCreateResponse;
import uni.backend.domain.dto.QnaResponse;
import uni.backend.domain.dto.QnaUserResponse;
import uni.backend.domain.dto.Response;
import uni.backend.service.PageTranslationService;
import uni.backend.service.QnaService;
import uni.backend.service.UserService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class QnaControllerTest {

    @Mock
    private QnaService qnaService;

    @Mock
    private UserService userService;

    @InjectMocks
    private QnaController qnaController;

    @Mock
    private Authentication authentication;

    @Mock
    private PageTranslationService pageTranslationService;

    private QnaCreateRequest qnaCreateRequest;
    private QnaResponse qnaResponse;
    private Qna qna;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        qnaCreateRequest = QnaCreateRequest.builder()
            .content("Sample content")
            .build();

        QnaUserResponse profileOwner = QnaUserResponse.builder()
            .userId(1)
            .name("Profile Owner")
            .build();
        QnaUserResponse commentAuthor = QnaUserResponse.builder()
            .userId(2)
            .name("Commenter")
            .build();

        qnaResponse = QnaResponse.builder()
            .qnaId(1)
            .profileOwner(profileOwner)
            .commentAuthor(commentAuthor)
            .content("Sample content")
            .build();

        user = new User();
        user.setUserId(2);
        user.setName("Commenter");

        qna = new Qna();
        qna.setQnaId(1);
        qna.setContent("Sample content");
    }

    @Test
    @DisplayName("acceptLanguage가 null일 때 Qna 목록 조회")
    void testGetUserQnasWithNullAcceptLanguage() {
        // given
        Integer userId = 1;
        String acceptLanguage = null;
        List<QnaResponse> qnaResponses = List.of(qnaResponse);
        when(qnaService.getUserQnas(userId)).thenReturn(qnaResponses);

        // when
        ResponseEntity<List<QnaResponse>> response = qnaController.getUserQnas(userId,
            acceptLanguage);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(qnaResponses, response.getBody());
        verify(qnaService, times(1)).getUserQnas(userId);
        verify(pageTranslationService, times(0)).translateQna(anyList(),
            anyString()); // translateQna가 호출되지 않음
    }

    @Test
    @DisplayName("acceptLanguage가 비어 있을 때 Qna 목록 조회")
    void testGetUserQnasWithEmptyAcceptLanguage() {
        // given
        Integer userId = 1;
        String acceptLanguage = "";
        List<QnaResponse> qnaResponses = List.of(qnaResponse);
        when(qnaService.getUserQnas(userId)).thenReturn(qnaResponses);

        // when
        ResponseEntity<List<QnaResponse>> response = qnaController.getUserQnas(userId,
            acceptLanguage);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(qnaResponses, response.getBody());
        verify(qnaService, times(1)).getUserQnas(userId);
        verify(pageTranslationService, times(0)).translateQna(anyList(),
            anyString()); // translateQna가 호출되지 않음
    }

    @Test
    @DisplayName("acceptLanguage가 'ko'일 때 Qna 목록 조회")
    void testGetUserQnasWithKoAcceptLanguage() {
        // given
        Integer userId = 1;
        String acceptLanguage = "ko";
        List<QnaResponse> qnaResponses = List.of(qnaResponse);
        when(qnaService.getUserQnas(userId)).thenReturn(qnaResponses);

        // when
        ResponseEntity<List<QnaResponse>> response = qnaController.getUserQnas(userId,
            acceptLanguage);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(qnaResponses, response.getBody());
        verify(qnaService, times(1)).getUserQnas(userId);
        verify(pageTranslationService, times(0)).translateQna(anyList(),
            anyString()); // translateQna가 호출되지 않음
    }

    @Test
    @DisplayName("acceptLanguage가 'en'일 때 Qna 목록 조회")
    void testGetUserQnasWithNonKoAcceptLanguage() {
        // given
        Integer userId = 1;
        String acceptLanguage = "en";
        List<QnaResponse> qnaResponses = List.of(qnaResponse);
        when(qnaService.getUserQnas(userId)).thenReturn(qnaResponses);

        // when
        ResponseEntity<List<QnaResponse>> response = qnaController.getUserQnas(userId,
            acceptLanguage);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(qnaResponses, response.getBody());
        verify(qnaService, times(1)).getUserQnas(userId);
        verify(pageTranslationService, times(1)).translateQna(anyList(),
            eq(acceptLanguage)); // translateQna가 호출됨
    }

    @Test
    @DisplayName("특정 유저의 Qna 작성")
    void testCreateQna() {
        // given
        Integer userId = 1;
        Integer commenterId = 2;
        QnaCreateRequest request = QnaCreateRequest.builder()
            .content("Sample content")
            .build();
        when(qnaService.createQna(userId, commenterId, request.getContent())).thenReturn(
            qnaResponse); // Mock 설정

        // when
        ResponseEntity<QnaCreateResponse> response = qnaController.createQna(userId, commenterId,
            request);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Qna가 성공적으로 작성되었습니다.", response.getBody().getMessage());
        verify(qnaService, times(1)).createQna(userId, commenterId, request.getContent());
    }

    @Test
    @DisplayName("Qna 삭제")
    void testDeleteQna() {
        // given
        Integer qnaId = 1;
        Qna qna = new Qna();
        qna.setQnaId(qnaId);
        qna.setContent("Sample content");

        when(qnaService.deleteQna(qnaId)).thenReturn(qna); // Mock 설정

        // when
        ResponseEntity<Response> response = qnaController.deleteQna(qnaId);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Qna가 삭제되었습니다.", response.getBody().getMessage());
        verify(qnaService, times(1)).deleteQna(qnaId); // deleteQna 메서드 호출 검증
    }
}