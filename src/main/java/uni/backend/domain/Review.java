package uni.backend.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;


    @OneToOne
    @JoinColumn(name = "matching_id", nullable = false)
    private Matching matching;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User profileOwner; // 프로필 주인 정보


    @ManyToOne
    @JoinColumn(name = "commenter_id", nullable = false)
    private User commenter; // 리뷰 작성자 정보

    @Column(nullable = false)
    private String content; // 리뷰 내용

    @Column(nullable = false)
    @Min(value = 1, message = "별점은 최소 1점이어야 합니다.")
    @Max(value = 5, message = "별점은 최대 5점이어야 합니다.")
    private Integer star; // 별점 (1~5)

    @Builder.Default
    @Column(nullable = false)
    private Long likes = 0L;
    private Boolean deleted = false; // 삭제 여부

    @Builder.Default
    @Column(nullable = false)
    private Boolean isBlind = false;

    public String getBlindReview() {
        return isBlind ? "이 리뷰는 블라인드 처리되었습니다." : content;
    }

    private LocalDateTime deletedTime; // 삭제 시간
    private LocalDateTime updatedTime; // 수정 시간

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewReply> replies; // ReviewReply와 연결


    public void increaseLikes() {
        this.likes++;
    }

    public void decreaseLikes() {
        this.likes--;
    }

    public void delete() {
        this.deleted = true;
        this.deletedTime = LocalDateTime.now();
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedTime = LocalDateTime.now(); // 수정 시간 업데이트
    }

    public void blindReview() {
        this.isBlind = true;
    }

    // 블라인드 해제
    public void unblindReview() {
        this.isBlind = false;
    }

}
