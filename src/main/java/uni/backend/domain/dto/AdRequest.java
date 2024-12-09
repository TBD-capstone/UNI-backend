package uni.backend.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uni.backend.enums.AdStatus;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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