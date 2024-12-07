package uni.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReportResponse {

    private String message;
    private LocalDateTime nextReportAllowedAt; // 다음 신고 가능 시간
}
