package uni.backend.domain.dto;

import lombok.*;
import uni.backend.domain.Matching;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingCreateResponse {
    private Integer requestId;
    private String status;

    public static MatchingCreateResponse from(Matching matching) {
        return MatchingCreateResponse.builder()
                .requestId(matching.getRequestId())
                .status(matching.getStatus().name())
                .build();
    }
}
