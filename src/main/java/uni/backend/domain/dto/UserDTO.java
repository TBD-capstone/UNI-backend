package uni.backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private String userId;
    private String imgProf;
    private String imgBack;
    private String region;
    private String univ;
    private String explain;
    private int numEmployment;
    private double star;
    private String time;

    // 기본 생성자, 필요에 따라 추가 생성자 작성 가능
}
