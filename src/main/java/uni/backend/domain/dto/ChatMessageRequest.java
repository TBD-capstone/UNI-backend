package uni.backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessageRequest {
    private String content; // 메시지 내용
    private Integer receiverId; // 받는 사람 ID
    private LocalDateTime sendAt;
}
