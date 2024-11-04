package uni.backend.domain.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingResponse {
    private Integer requestId;
    private boolean accepted;
}
