package uni.backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProfileDTO {
    private UserDTO user;
    private List<ReviewDTO> review;

    // 기본 생성자, 필요에 따라 추가 생성자 작성 가능
}
