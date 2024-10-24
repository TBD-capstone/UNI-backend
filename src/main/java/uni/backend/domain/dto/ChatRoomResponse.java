package uni.backend.domain.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomResponse {
    private Integer chatRoomId;
    private List<ChatMessageResponse> chatMessages;
    private Integer myId;
    private Integer otherId;
}
