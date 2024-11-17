package uni.backend.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class HomeProfileResponse {

    private String username;
    private String imgProf;
    private Double star;
    private String univName;
    private List<String> hashtags;
    private Integer userId;

}
