package uni.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uni.backend.enums.AdStatus;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ad_id")
    private Integer adId;

    private String advertiser;
    private String title;

    @Column(nullable = false)
    private AdStatus adStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private String imageUrl;
//    private String description;

}


/*
Ad: {
"adId": int, "advertiser":string,
"title": string,
"adStatus": ENUM,
"startDate": DateTime,
"endDate": DateTime,
"imageUrl": string

adStatus: (posted|ended)
}


 */