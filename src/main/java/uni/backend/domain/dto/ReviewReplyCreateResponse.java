package uni.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewReplyCreateResponse {

    private String status; // "success" 또는 "fail"
    private String message; // 성공 또는 실패 메시지
    private ReviewReplyResponse reply; // 작성된 ReviewReply 정보

    public static ReviewReplyCreateResponse success(String message, ReviewReplyResponse reply) {
        return new ReviewReplyCreateResponse("success", message, reply);
    }

    public static ReviewReplyCreateResponse fail(String message) {
        return new ReviewReplyCreateResponse("fail", message, null);
    }
}
