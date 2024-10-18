package uni.backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatRoomRequest {
    private String otherUserEmail; // 상대방 이메일을 전달
    private LocalDateTime createdAt;
}
