package uni.backend.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ChatMainResponse {
    private Integer chatRoomId;
    private String otherName; // 상대방의 이름
    private LocalDateTime lastMessageAt; // 마지막 메시지 시간
}
