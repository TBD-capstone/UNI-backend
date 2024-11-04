package uni.backend.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMainResponse {

    private Integer chatRoomId;
    private String otherName;
    private LocalDateTime lastMessageTime;
}
