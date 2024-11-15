package uni.backend.domain.dto;

import lombok.*;
import uni.backend.domain.Matching;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingListResponse {
    private Integer matchingId;
    private Integer requesterId;
    private Integer receiverId;
    private String status;
    private LocalDateTime createdAt;

    public static MatchingListResponse fromMatching(Matching matching) {
        return MatchingListResponse.builder()
                .matchingId(matching.getRequestId())
                .requesterId(matching.getRequester().getUserId())
                .receiverId(matching.getReceiver().getUserId())
                .status(matching.getStatus().name())
                .createdAt(matching.getCreatedAt())
                .build();
    }
}
