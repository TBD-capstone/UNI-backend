package uni.backend.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReplyCreateRequest {

    private String content; // 대댓글 본문
}
