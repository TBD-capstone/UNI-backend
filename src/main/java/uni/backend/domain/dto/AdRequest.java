package uni.backend.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import uni.backend.enums.AdStatus;

@Data
@Builder
public class AdRequest {

    String advertiser;
    String title;
    String adStatus;
    LocalDate startDate;
    LocalDate endDate;
    String imageUrl;
}


/*

{
"advertiser":string,
"title": string,
"adStatus": ENUM,
"startDate": DateTime,
"endDate": DateTime,
"imageUrl": string
}


 */