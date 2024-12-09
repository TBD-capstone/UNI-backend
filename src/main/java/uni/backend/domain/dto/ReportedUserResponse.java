package uni.backend.domain.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportedUserResponse {

    private Integer userId;
    private String email;
    private Long reportCount;
    private List<ReportDetail> reports; // 신고 세부 정보 리스트

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReportDetail {

        private Integer reportId;        // 신고 ID
        private String title;            // 신고 제목
        private String category;         // 신고 카테고리
        private String reason;           // 신고 사유
        private String detailedReason;   // 상세 신고 사유
        private LocalDateTime reportedAt; // 신고 날짜
        private String reporterName;     // 신고한 사람 이름
        private String reportedUserName; // 신고당한 사람 이름
    }
}
