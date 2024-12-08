package uni.backend.domain.dto;

import lombok.Builder;
import lombok.Data;
import uni.backend.enums.AdStatus;

@Builder
@Data
public class UpdateAdStatusRequest {

    private Integer adId;
    private AdStatus newStatus;
}
