package uni.backend.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewReplyCreateRequest {

    private String content; // 단순히 content 필드만 처리
}
