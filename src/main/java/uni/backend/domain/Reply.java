package uni.backend.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer replyId;

    @ManyToOne
    @JoinColumn(name = "qna_id", nullable = false)
    private Qna qna;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User commenter;

    @Column(nullable = false)
    private String content; // 대댓글 본문

    private Long likes = 0L; // 좋아요 수

    @Column(nullable = false)
    private Boolean deleted = false; // 삭제 여부

    @Column(nullable = false)
    private Boolean isBlind = false; // 블라인드 여부

    private LocalDateTime deletedTime; // 삭제 시간
    private LocalDateTime updatedTime; // 수정 시간

    // 소프트 삭제 메서드
    public void softDelete() {
        this.deleted = true;
        this.deletedTime = LocalDateTime.now(); // 삭제 시간 업데이트
    }

    // 좋아요 수 증가 메서드
    public void increaseLikes() {
        this.likes++;
    }

    // 좋아요 수 감소 메서드
    public void decreaseLikes() {
        this.likes--;
    }

    // 대댓글 본문 수정 메서드
    public void updateContent(String content) {
        this.content = content;
        this.updatedTime = LocalDateTime.now(); // 수정 시간 업데이트
    }

    public Reply(Qna qna, User commenter, String content) {
        this.qna = qna;
        this.commenter = commenter;
        this.content = content;
    }


    // 블라인드 처리 메서드
    public void blindReply() {
        this.isBlind = true;
    }

    // 블라인드 해제 메서드
    public void unblindReply() {
        this.isBlind = false;
    }

    
    public Reply() {
    }
}
