package uni.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uni.backend.domain.Marker;
import uni.backend.domain.User;
import uni.backend.domain.dto.MarkerRequest;
import uni.backend.domain.dto.MarkerResponse;
import uni.backend.repository.MarkerRepository;
import uni.backend.repository.UserRepository;

class MarkerServiceTest {

    @InjectMocks
    private MarkerService markerService;

    @Mock
    private MarkerRepository markerRepository;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 마커_추가_성공() {
        // Given
        Integer userId = 1;
        MarkerRequest markerRequest = new MarkerRequest();
        markerRequest.setLatitude(37.5665);
        markerRequest.setLongitude(126.9780);
        markerRequest.setName("Test Marker");
        markerRequest.setDescription("Test Description");

        User user = new User();
        user.setUserId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(markerRepository.save(any(Marker.class))).thenAnswer(
            invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> markerService.addMarker(markerRequest, userId));

        // Then
        verify(userRepository).findById(userId);
        verify(markerRepository).save(any(Marker.class));
    }

    @Test
    void 마커_추가_실패_유저없음() {
        // Given
        Integer userId = 1;
        MarkerRequest markerRequest = new MarkerRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            markerService.addMarker(markerRequest, userId);
        });

        assertEquals("User not found with id: " + userId, exception.getMessage());
        verify(userRepository).findById(userId);
        verify(markerRepository, never()).save(any(Marker.class));
    }

    @Test
    void 마커_수정_성공() {
        // Given
        Integer markerId = 1;
        MarkerRequest markerRequest = new MarkerRequest();
        markerRequest.setLatitude(37.5665);
        markerRequest.setLongitude(126.9780);
        markerRequest.setName("Updated Marker");
        markerRequest.setDescription("Updated Description");

        Marker existingMarker = new Marker();
        existingMarker.setId(markerId);

        when(markerRepository.findById(markerId)).thenReturn(Optional.of(existingMarker));
        when(markerRepository.save(any(Marker.class))).thenAnswer(
            invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> markerService.updateMarker(markerId, markerRequest));

        // Then
        assertEquals("Updated Marker", existingMarker.getName());
        assertEquals("Updated Description", existingMarker.getDescription());
        verify(markerRepository).findById(markerId);
        verify(markerRepository).save(existingMarker);
    }

    @Test
    void 마커_수정_실패_마커없음() {
        // Given
        Integer markerId = 1;
        MarkerRequest markerRequest = new MarkerRequest();

        when(markerRepository.findById(markerId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            markerService.updateMarker(markerId, markerRequest);
        });

        assertEquals("Marker not found", exception.getMessage());
        verify(markerRepository).findById(markerId);
        verify(markerRepository, never()).save(any(Marker.class));
    }

    @Test
    void 마커_삭제_성공() {
        // Given
        Integer markerId = 1;

        doNothing().when(markerRepository).deleteById(markerId);

        // When
        assertDoesNotThrow(() -> markerService.deleteMarker(markerId));

        // Then
        verify(markerRepository).deleteById(markerId);
    }

    @Test
    void 사용자_마커_조회_성공() {
        // Given
        Integer userId = 1;

        List<Marker> markers = new ArrayList<>();
        Marker marker1 = new Marker();
        marker1.setId(1);
        marker1.setLatitude(37.5665);
        marker1.setLongitude(126.9780);
        marker1.setName("Marker 1");
        marker1.setDescription("Description 1");

        Marker marker2 = new Marker();
        marker2.setId(2);
        marker2.setLatitude(37.5670);
        marker2.setLongitude(126.9790);
        marker2.setName("Marker 2");
        marker2.setDescription("Description 2");

        markers.add(marker1);
        markers.add(marker2);

        when(markerRepository.findByUser_UserId(userId)).thenReturn(markers);

        // When
        List<MarkerResponse> responses = markerService.getUserMarkers(userId);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Marker 1", responses.get(0).getName());
        assertEquals("Marker 2", responses.get(1).getName());
        verify(markerRepository).findByUser_UserId(userId);
    }
}
