package uni.backend.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uni.backend.domain.ReportCategory;
import uni.backend.domain.ReportReason;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequest {

    private Integer reportedUserId;   // 신고된 유저의 ID
    private Integer reporterUserId;   // 신고한 유저의 ID
    private ReportReason reason;      // 신고 사유 (열거형)
    private String detailedReason;    // 상세 신고 사유
    private ReportCategory category;  // 신고 카테고리 (열거형)
}
