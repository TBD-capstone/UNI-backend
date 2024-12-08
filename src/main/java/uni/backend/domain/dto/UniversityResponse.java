package uni.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor // Lombok을 사용해 모든 필드의 생성자 자동 생성
@NoArgsConstructor  // Lombok을 사용해 기본 생성자 자동 생성
@Builder
public class UniversityResponse {

    private Integer universityId;
    private String univName;
    private String enUnivName;
}
