package uni.backend.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import uni.backend.enums.AdStatus;

@Data
public class AdResponse {

    Integer adId;
    String advertiser;
    String title;
    String adStatus;
    LocalDate startDate;
    LocalDate endDate;
    String imageUrl;
}