package uni.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class ReviewReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer replyId;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private Review review; // Review와의 관계 설정

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User commenter; // 대댓글 작성자

    @Column(nullable = false)
    private String content; // 대댓글 본문

    @Builder.Default
    private Long likes = 0L; // 좋아요 수

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false; // 삭제 여부

    @Builder.Default
    @Column(nullable = false)
    private Boolean isBlind = false;

    private LocalDateTime deletedTime; // 삭제 시간
    private LocalDateTime updatedTime; // 수정 시간


    // 블라인드 처리 메서드
    public void blindReply() {
        this.isBlind = true;
    }

    // 블라인드 해제 메서드
    public void unblindReply() {
        this.isBlind = false;
    }

    public String getBlindReviewReply() {
        return isBlind ? "이 대댓글은 블라인드 처리되었습니다." : content;
    }


    public void softDelete() {
        this.deleted = true;
        this.deletedTime = LocalDateTime.now();
    }

    public void increaseLikes() {
        this.likes++;
    }

    public void decreaseLikes() {
        this.likes--;
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedTime = LocalDateTime.now();
    }

    public ReviewReply(Review review, User commenter, String content) {
        this.review = review;
        this.commenter = commenter;
        this.content = content;
    }

}
