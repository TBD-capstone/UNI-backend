package uni.backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplyCreateRequest {

  private String content; // 대댓글 본문
}
