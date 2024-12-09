package uni.backend.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class QnaCreateRequest {

    private String content; // 댓글 내용
}
