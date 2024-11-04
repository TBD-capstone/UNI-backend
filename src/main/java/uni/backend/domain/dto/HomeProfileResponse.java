package uni.backend.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class HomeProfileResponse {

     String username;
     String imgProf;
     Double star;
     String univName;
     List<String> hashtags;
     Integer userId;

}
