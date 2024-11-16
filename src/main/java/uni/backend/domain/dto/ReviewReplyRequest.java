package uni.backend.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewReplyRequest {

    @NotNull(message = "대댓글 내용은 필수입니다.")
    private String content; // 대댓글 내용
}