package uni.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchingResponse {

    private Integer matchingId;
    private Integer profileOwnerId;
    private Integer requesterId;
}