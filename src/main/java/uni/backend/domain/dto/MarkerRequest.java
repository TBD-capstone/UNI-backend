package uni.backend.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkerRequest {


    private double latitude;
    private double longitude;
    private String name;
    private String description;
}
