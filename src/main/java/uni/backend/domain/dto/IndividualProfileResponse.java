package uni.backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
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
    private List<String> hashtags = new ArrayList<>(); // 해시태그 목록
//  private List<QnaResponse> qnas;

//    private List<MainCategoryDTO> mainCategories = new ArrayList<>();  // 메인 카테고리 리스트

}


