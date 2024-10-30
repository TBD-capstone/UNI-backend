package uni.backend.domain.dto;

import lombok.Data;

@Data
public class HomeProfileResponse {
     String username;
     String imgProf;
     Double star;
     String univName;
     String[] hashtags;
}
