package uni.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
public class LoginResponse {

    private String status;
    private String message;
    private String userName;
    private Integer userId;
    private Boolean isKorean;
    private String imgProf;
    private String imgBack;
    private String accessToken;
    private String refreshToken;
}

