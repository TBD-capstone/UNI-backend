package uni.backend.service;

import jakarta.persistence.criteria.CriteriaBuilder.In;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import uni.backend.domain.Marker;
import uni.backend.domain.User;
import uni.backend.domain.dto.MarkerRequest;
import uni.backend.domain.dto.MarkerResponse;
import uni.backend.repository.MarkerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uni.backend.repository.UserRepository;

@Service
public class MarkerService {

    private final MarkerRepository markerRepository;
    private final UserRepository userRepository;  // UserRepository 추가

    @Autowired
    public MarkerService(MarkerRepository markerRepository, UserRepository userRepository) {
        this.markerRepository = markerRepository;
        this.userRepository = userRepository;
    }

    // 마커 추가
    public void addMarker(MarkerRequest markerRequest, Integer userId) {
        // userId를 사용하여 User 객체를 조회합니다.
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Marker marker = new Marker();
        marker.setUser(user);  // User 객체를 설정
        marker.setLatitude(markerRequest.getLatitude());
        marker.setLongitude(markerRequest.getLongitude());
        marker.setName(markerRequest.getName());
        marker.setDescription(markerRequest.getDescription());

        markerRepository.save(marker);
    }


    // 마커 수정
    public void updateMarker(Integer markerId, MarkerRequest markerRequest) {
        Marker marker = markerRepository.findById(markerId)
            .orElseThrow(() -> new IllegalArgumentException("Marker not found"));
        marker.setLatitude(markerRequest.getLatitude());
        marker.setLongitude(markerRequest.getLongitude());
        marker.setName(markerRequest.getName());
        marker.setDescription(markerRequest.getDescription());

        markerRepository.save(marker);
    }

    // 마커 삭제
    public void deleteMarker(Integer markerId) {
        markerRepository.deleteById(markerId);
    }

    // 사용자 마커 조회
    public List<MarkerResponse> getUserMarkers(Integer userId) {
        List<Marker> markers = markerRepository.findByUser_UserId(userId);
        return markers.stream()
            .map(marker -> MarkerResponse.builder()
                .id(marker.getId())
                .latitude(marker.getLatitude())
                .longitude(marker.getLongitude())
                .name(marker.getName())
                .description(marker.getDescription())
                .build())
            .collect(Collectors.toList());
    }
}
