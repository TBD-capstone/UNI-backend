package uni.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Integer profileId;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

//    private String content;

    // 추가 필드
    private String imgProf;  // 프로필 이미지
    private String imgBack;  // 배경 이미지
    private String region;          // 지역
    private String description;         // 설명
    private int numEmployment;      // 고용 횟수
    private double star;            // 별점
    private String time;            // 활동시간

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MainCategory> mainCategories = new ArrayList<>();

    public void addMainCategory(MainCategory mainCategory) {
        this.mainCategories.add(mainCategory);
        mainCategory.setProfile(this); // 양방향 연관관계 설정
    }

    public void removeMainCategory(MainCategory mainCategory) {
        mainCategories.remove(mainCategory);
        mainCategory.setProfile(null);
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now(); // 프로필 생성 시 작성일 설정
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now(); // 프로필 수정 시 수정일 업데이트
    }
}
