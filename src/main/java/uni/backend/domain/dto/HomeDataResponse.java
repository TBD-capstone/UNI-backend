package uni.backend.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class HomeDataResponse {

    //    String profileString;
    private List<HomeProfileResponse> data;
}
