package uni.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import uni.backend.domain.dto.MarkerRequest;
import uni.backend.domain.dto.MarkerResponse;
import uni.backend.domain.dto.Response;
import uni.backend.service.MarkerService;
import uni.backend.service.PageTranslationService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MarkerControllerTest {

    @InjectMocks
    private MarkerController markerController;

    @Mock
    private MarkerService markerService;

    @Mock
    private PageTranslationService pageTranslationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addMarker_shouldReturnSuccessMessage() {
        // given
        Integer userId = 1;
        MarkerRequest markerRequest = new MarkerRequest();

        // when
        ResponseEntity<Response> response = markerController.addMarker(userId, markerRequest);

        // then
        verify(markerService, times(1)).addMarker(markerRequest, userId);
        assertEquals("마커 추가 완료", response.getBody().getMessage());
    }

    @Test
    void updateMarker_shouldReturnSuccessMessage() {
        // given
        Integer markerId = 1;
        MarkerRequest markerRequest = new MarkerRequest();

        // when
        ResponseEntity<Response> response = markerController.updateMarker(markerId, markerRequest);

        // then
        verify(markerService, times(1)).updateMarker(markerId, markerRequest);
        assertEquals("마커 업데이트 완료", response.getBody().getMessage());
    }

    @Test
    void deleteMarker_shouldReturnSuccessMessage() {
        // given
        Integer markerId = 1;

        // when
        ResponseEntity<Response> response = markerController.deleteMarker(markerId);

        // then
        verify(markerService, times(1)).deleteMarker(markerId);
        assertEquals("마커 삭제 완료", response.getBody().getMessage());
    }

    @Test
    void getUserMarkers_shouldReturnMarkerListWithoutTranslation() {
        // given
        Integer userId = 1;
        String acceptLanguage = null;
        List<MarkerResponse> markerResponses = Arrays.asList(new MarkerResponse(),
            new MarkerResponse());
        when(markerService.getUserMarkers(userId)).thenReturn(markerResponses);

        // when
        ResponseEntity<List<MarkerResponse>> response = markerController.getUserMarkers(userId,
            acceptLanguage);

        // then
        verify(markerService, times(1)).getUserMarkers(userId);
        verify(pageTranslationService, never()).translateMarkers(anyList(), anyString());
        assertEquals(markerResponses, response.getBody());
    }

    @Test
    void getUserMarkers_shouldReturnMarkerListWithTranslationWhenLanguageProvidedAndMarkersExist() {
        // given
        Integer userId = 1;
        String acceptLanguage = "en";
        List<MarkerResponse> markerResponses = Arrays.asList(new MarkerResponse(),
            new MarkerResponse());
        when(markerService.getUserMarkers(userId)).thenReturn(markerResponses);

        // when
        ResponseEntity<List<MarkerResponse>> response = markerController.getUserMarkers(userId,
            acceptLanguage);

        // then
        verify(markerService, times(1)).getUserMarkers(userId);
        verify(pageTranslationService, times(1)).translateMarkers(markerResponses, acceptLanguage);
        assertEquals(markerResponses, response.getBody());
    }

    @Test
    void getUserMarkers_shouldNotTranslateWhenLanguageProvidedButMarkersAreEmpty() {
        // given
        Integer userId = 1;
        String acceptLanguage = "en";
        List<MarkerResponse> markerResponses = Arrays.asList();
        when(markerService.getUserMarkers(userId)).thenReturn(markerResponses);

        // when
        ResponseEntity<List<MarkerResponse>> response = markerController.getUserMarkers(userId,
            acceptLanguage);

        // then
        verify(markerService, times(1)).getUserMarkers(userId);
        verify(pageTranslationService, never()).translateMarkers(anyList(), anyString());
        assertEquals(markerResponses, response.getBody());
    }
}