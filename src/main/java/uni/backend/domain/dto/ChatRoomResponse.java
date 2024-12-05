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
    private String myName;
    private String myImgProf;
    private Integer otherId;
    private String otherName;
    private String otherImgProf;
    private long unreadCount;
}
