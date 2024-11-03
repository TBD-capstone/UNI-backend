package uni.backend.domain.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingRequest {
    private Integer requestId;
    private Integer requesterId;
    private Integer receiverId;
}
