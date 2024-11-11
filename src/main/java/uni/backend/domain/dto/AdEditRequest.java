package uni.backend.domain.dto;

import lombok.Data;
import uni.backend.enums.AdStatus;

@Data
public class AdEditRequest {

    Integer adId;
    String adStatus;
}
