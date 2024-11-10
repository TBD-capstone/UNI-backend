package uni.backend.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequest {

    private Integer roomId;
    private String content;
    private Integer senderId;
    private Integer receiverId;
    private LocalDateTime sendAt;
}
