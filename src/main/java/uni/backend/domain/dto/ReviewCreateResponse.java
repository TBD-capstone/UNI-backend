package uni.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewCreateResponse {

    private String status; // "success" 또는 "fail"
    private String message; // 성공 또는 실패 메시지
    private ReviewResponse review; // 작성된 Review 정보

    public static ReviewCreateResponse success(String message, ReviewResponse review) {
        return new ReviewCreateResponse("success", message, review);
    }

    public static ReviewCreateResponse fail(String message) {
        return new ReviewCreateResponse("fail", message, null);
    }
}
