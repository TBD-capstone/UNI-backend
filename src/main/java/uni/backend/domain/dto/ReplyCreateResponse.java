package uni.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReplyCreateResponse {

    private String status;
    private String message;
    private ReplyResponse reply; // 대댓글 정보

    public static ReplyCreateResponse success(String message, ReplyResponse reply) {
        return new ReplyCreateResponse("success", message, reply);
    }
}
