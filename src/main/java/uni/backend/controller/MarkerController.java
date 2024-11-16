package uni.backend.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.dto.MarkerRequest;
import uni.backend.domain.dto.MarkerResponse;
import uni.backend.domain.dto.Response;
import uni.backend.service.MarkerService;

@RestController
@RequestMapping("/api/markers")
public class MarkerController {

    private final MarkerService markerService;

    @Autowired
    public MarkerController(MarkerService markerService) {
        this.markerService = markerService;
    }

    // 마커 추가 API
    @PostMapping("/add/{userId}")
    public ResponseEntity<Response> addMarker(@PathVariable Integer userId,
        @RequestBody MarkerRequest markerRequest) {
        markerService.addMarker(markerRequest, userId);
        return ResponseEntity.ok(Response.successMessage("마커 추가 완료"));
    }


    // 마커 수정 API
    @PutMapping("/updates/{markerId}")
    public ResponseEntity<Response> updateMarker(@PathVariable Integer markerId,
        @RequestBody MarkerRequest markerRequest) {
        markerService.updateMarker(markerId, markerRequest);
        return ResponseEntity.ok(Response.successMessage("마커 업데이트 완료"));
    }

    // 마커 삭제 API
    @DeleteMapping("/delete/{markerId}")
    public ResponseEntity<Response> deleteMarker(@PathVariable Integer markerId) {
        markerService.deleteMarker(markerId);
        return ResponseEntity.ok(Response.successMessage("마커 삭제 완료"));

    }

    // 사용자 마커 조회 API
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MarkerResponse>> getUserMarkers(@PathVariable Integer userId) {
        List<MarkerResponse> markers = markerService.getUserMarkers(userId);
        return ResponseEntity.ok(markers);
    }
}
