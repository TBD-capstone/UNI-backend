package uni.backend.domain.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uni.backend.domain.ReportCategory;
import uni.backend.domain.ReportReason;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequest {

    private Integer reportedUserId;   // 신고된 유저의 ID
    private Integer reporterUserId;   // 신고한 유저의 ID
    private ReportReason reason;      // 신고 사유 (열거형)
    private ReportCategory category;  // 신고 카테고리 (열거형)

    @NotBlank
    @Size(min = 5, message = "신고 제목은 최소 5자 이상이어야 합니다.")
    private String title; // 신고 제목 추가

    @NotBlank
    @Size(min = 10, message = "신고 사유는 최소 10자 이상이어야 합니다.")
    private String detailedReason; // 신고 상세 사유
}
