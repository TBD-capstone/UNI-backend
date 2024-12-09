package uni.backend.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.dto.ReportRequest;
import uni.backend.service.ReportService;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;


    /**
     * 특정 유저에 대한 신고 생성
     *
     * @param userId        신고할 유저의 ID
     * @param reportRequest 신고 요청 데이터
     * @return 처리 결과 메시지 및 다음 신고 가능 시간
     */
    @PostMapping("/{userId}/report")
    public ResponseEntity<Map<String, Object>> createReport(
        @PathVariable Integer userId,
        @RequestBody ReportRequest reportRequest) {
        Map<String, Object> response = reportService.createReport(userId, reportRequest);
        return ResponseEntity.ok(response);
    }

}
