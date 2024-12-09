package uni.backend.domain.dto;

import lombok.*;
import uni.backend.domain.Matching;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingUpdateResponse {

    private Integer matchingId;
    private String status;
    private Integer requesterId;

    public static MatchingUpdateResponse from(Matching matching) {
        return MatchingUpdateResponse.builder()
            .matchingId(matching.getMatchingId())
            .status(matching.getStatus().name())
            .requesterId(matching.getRequester().getUserId())
            .build();
    }
}
