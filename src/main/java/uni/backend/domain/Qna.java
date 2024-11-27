package uni.backend.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Qna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer qnaId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User profileOwner; // 프로필 주인 정보


    @ManyToOne
    @JoinColumn(name = "commenter_id", nullable = false)
    private User commenter; // 댓글 작성자 정보

    @Column(nullable = false)
    private String content; // 댓글 내용

    private Long likes = 0L; // 좋아요 수

    private Boolean deleted = false; // 삭제 여부
    private LocalDateTime deletedTime; // 삭제 시간
    private LocalDateTime updatedTime; // 수정 시간 추가

    @OneToMany(mappedBy = "qna", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reply> replies; // 대댓글 리스트

    @Column(nullable = false)
    private boolean isBlind = false; // 블라인드 여부

    public String getBlindQna() {
        return isBlind ? "이 QnA는 블라인드 처리되었습니다." : content;
    }

    public void blindQna() {
        this.isBlind = true;
    }

    public void unblindQna() {
        this.isBlind = false;
    }

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

    // 댓글 본문 수정 메서드
    public void updateContent(String content) {
        this.content = content;
        this.updatedTime = LocalDateTime.now(); // 수정 시간 업데이트
    }
}
