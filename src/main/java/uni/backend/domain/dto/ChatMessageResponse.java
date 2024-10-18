package uni.backend.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ChatMessageResponse {
    private String content; // 메시지 내용
    private Integer senderId; // 보낸 사람의 ID
    private LocalDateTime sendAt; // 보낸 시간
}
