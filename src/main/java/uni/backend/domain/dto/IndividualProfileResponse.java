package uni.backend.domain.dto;

import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import uni.backend.domain.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class IndividualProfileResponse {

    private Integer userId;        // User ID
    private String userName;       // User Name
    private String imgProf;        // 프로필 이미지
    private String imgBack;        // 배경 이미지
    private String univ;           // 대학교
    private String region;         // 지역
    private String description;        // 설명
    private int numEmployment;     // 고용 횟수
    private double star;           // 별점
    private String time;           // 시간
    @Builder.Default
    private boolean isVisible = true;
    private List<String> hashtags = new ArrayList<>(); // 해시태그 목록
    private Role role;  // ENUM 타입 추가


    public static IndividualProfileResponse createDefault() {
        return IndividualProfileResponse.builder()
            .hashtags(new ArrayList<>()) // 기본 빈 리스트 설정
            .build();
    }
//  private List<QnaResponse> qnas;

//    private List<MainCategoryDTO> mainCategories = new ArrayList<>();  // 메인 카테고리 리스트

}


