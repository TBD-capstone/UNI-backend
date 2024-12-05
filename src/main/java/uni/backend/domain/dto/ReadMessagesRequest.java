package uni.backend.domain.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadMessagesRequest {
    private Integer roomId;
    private List<Integer> messageIds;
}

