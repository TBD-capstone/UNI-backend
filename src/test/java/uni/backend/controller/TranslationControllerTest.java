package uni.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uni.backend.domain.dto.*;
import uni.backend.service.TranslationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TranslationControllerTest {

    @Mock
    private TranslationService translationService;

    @InjectMocks
    private TranslationController translationController;

    @Mock
    private HttpServletRequest httpRequest;

    @BeforeEach
    void setUp() {
        translationController = new TranslationController();
        ReflectionTestUtils.setField(translationController, "translationService",
            translationService);
    }

    @Test
    @DisplayName("단순 번역 테스트")
    void testTranslate() {
        // given
        TranslationRequest request = new TranslationRequest();
        request.setText(List.of("안녕하세요"));
        request.setSource_lang("ko");
        request.setTarget_lang("en");

        TranslationResponse translationResponse = new TranslationResponse();
        IndividualTranslationResponse translation = new IndividualTranslationResponse();
        translation.setText("Hello");
        translationResponse.setTranslations(List.of(translation));

        when(translationService.determineTargetLanguage(any())).thenReturn("en");
        when(translationService.translate(any(TranslationRequest.class))).thenReturn(
            translationResponse);

        // when
        ResponseEntity<TranslationResponse> response = translationController.translate(request,
            httpRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Hello", response.getBody().getTranslations().get(0).getText());
        verify(translationService, times(1)).translate(any(TranslationRequest.class));
    }

    /*@Test
    @DisplayName("용어집 생성 테스트")
    void testCreateGlossary() {
        // given
        CreateGlossaryRequest request = new CreateGlossaryRequest();
        request.setName("test glossary");
        request.setSource_lang("ko");
        request.setTarget_lang("en");
        request.setEntries("test_entry");
        request.setEntries_format("json");

        CreateGlossaryResponse createGlossaryResponse = new CreateGlossaryResponse();
        createGlossaryResponse.setGlossary_id("1");
        createGlossaryResponse.setName("test glossary");
        createGlossaryResponse.setReady(true);
        createGlossaryResponse.setSource_lang("ko");
        createGlossaryResponse.setTarget_lang("en");
        createGlossaryResponse.setCreation_time(LocalDateTime.now());
        createGlossaryResponse.setEntry_count(1);

        when(translationService.createGlossary(any(CreateGlossaryRequest.class))).thenReturn(
            createGlossaryResponse);

        // when
        ResponseEntity<CreateGlossaryResponse> response = translationController.createGlossary(
            request);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("test glossary", response.getBody().getName());
        verify(translationService, times(1)).createGlossary(any(CreateGlossaryRequest.class));
    }*/

    /*@Test
    @DisplayName("용어집 목록 조회 테스트")
    void testListGlossaries() {
        // given
        GlossaryResponse glossaryResponse = new GlossaryResponse();
        glossaryResponse.setGlossary_id("1");
        glossaryResponse.setReady(true);
        glossaryResponse.setName("test glossary");
        glossaryResponse.setSource_lang("ko");
        glossaryResponse.setTarget_lang("en");
        glossaryResponse.setCreation_time(LocalDateTime.now());
        glossaryResponse.setEntry_count(1);

        GlossariesListResponse glossariesListResponse = new GlossariesListResponse();
        glossariesListResponse.setGlossaries(List.of(glossaryResponse));

        when(translationService.getGlossariesList()).thenReturn(glossariesListResponse);

        // when
        ResponseEntity<GlossariesListResponse> response = translationController.listGlossaries();

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getGlossaries().size());
        verify(translationService, times(1)).getGlossariesList();
    }*/

    /*@Test
    @DisplayName("용어집 조회 테스트")
    void testRetrieveGlossary() {
        // given
        String glossaryId = "1";
        SingleGlossaryResponse singleGlossaryResponse = new SingleGlossaryResponse();
        singleGlossaryResponse.setMessage("Glossary found");
        singleGlossaryResponse.setDetail("Glossary details");

        when(translationService.retrieveGlossaryEntry(anyString())).thenReturn(
            singleGlossaryResponse);

        // when
        ResponseEntity<SingleGlossaryResponse> response = translationController.retrieveGlossaryEntry(
            glossaryId);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Glossary found", response.getBody().getMessage());
        assertEquals("Glossary details", response.getBody().getDetail());
        verify(translationService, times(1)).retrieveGlossaryEntry(anyString());
    }*/
}