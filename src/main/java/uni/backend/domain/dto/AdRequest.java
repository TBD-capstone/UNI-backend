package uni.backend.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import uni.backend.enums.AdStatus;

@Data
@Builder
public class AdRequest {

    private String advertiser;
    private String title;
    private AdStatus adStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private String imageUrl;

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