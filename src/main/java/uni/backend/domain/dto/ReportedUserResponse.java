package uni.backend.domain.dto;

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

        private String title;
        private String category;      // 신고 카테고리 (PROFILE, CHAT, QNA, REVIEW 등)
        private String reason;        // 신고 사유 (욕설/혐오/차별, 음란물 등)
        private String detailedReason; // 상세 신고 사유
    }
}
