package uni.backend.domain.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingUpdateRequest {
    private Integer requestId;
    private boolean accepted;
}
