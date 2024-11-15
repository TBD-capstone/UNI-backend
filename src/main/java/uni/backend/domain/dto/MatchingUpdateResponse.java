package uni.backend.domain.dto;

import lombok.*;
import uni.backend.domain.Matching;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingUpdateResponse {
    private Integer requestId;
    private String status;

    public static MatchingUpdateResponse from(Matching matching) {
        return MatchingUpdateResponse.builder()
                .requestId(matching.getRequestId())
                .status(matching.getStatus().name())
                .build();
    }
}
