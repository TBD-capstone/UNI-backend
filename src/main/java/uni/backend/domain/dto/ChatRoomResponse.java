package uni.backend.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ChatRoomResponse {
    private Integer chatRoomId;
    private List<ChatMessageResponse> chatMessages; // 채팅 메시지 목록
    private Integer myId; // 내 아이디
    private Integer otherId; // 상대방 아이디
}
