package uni.backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDTO {
    private String comment;
    private String reviewerId;
    private String time;

    // 기본 생성자, 필요에 따라 추가 생성자 작성 가능
}
