package uni.backend.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.dto.MarkerRequest;
import uni.backend.domain.dto.MarkerResponse;
import uni.backend.domain.dto.Response;
import uni.backend.service.MarkerService;
import uni.backend.service.PageTranslationService;

@RestController
@RequestMapping("/api/markers")
public class MarkerController {

    private final MarkerService markerService;

    private final PageTranslationService pageTranslationService;

    @Autowired
    public MarkerController(MarkerService markerService,
        PageTranslationService pageTranslationService) {
        this.markerService = markerService;
        this.pageTranslationService = pageTranslationService;
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
    public ResponseEntity<List<MarkerResponse>> getUserMarkers(@PathVariable Integer userId,
        @RequestHeader(name = "Accept-Language", required = false) String acceptLanguage) {
        List<MarkerResponse> markers = markerService.getUserMarkers(userId);

        if (acceptLanguage != null && !acceptLanguage.isEmpty() && !markers.isEmpty()) {
            pageTranslationService.translateMarkers(markers, acceptLanguage);
        }
        return ResponseEntity.ok(markers);
    }
}
