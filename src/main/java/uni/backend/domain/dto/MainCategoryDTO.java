package uni.backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MainCategoryDTO {
    private Integer mainCategoryId; // 카테고리 ID
    private Integer hashtagId; // 해시태그 ID
    private String name;        // 카테고리 이름 추가
}
