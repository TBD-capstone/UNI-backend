package uni.backend.controller;

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
     * @return 처리 결과 메시지
     */
    @PostMapping("/{userId}/report")
    public ResponseEntity<String> createReport(
        @PathVariable Integer userId,
        @RequestBody ReportRequest reportRequest) {
        reportService.createReport(userId, reportRequest);
        return ResponseEntity.ok("신고가 성공적으로 접수되었습니다.");
    }
}
