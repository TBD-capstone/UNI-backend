package uni.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "review_reply_likes")
public class ReviewReplyLikes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "review_reply_id", nullable = false)
    private ReviewReply reviewReply; // 좋아요가 달린 대댓글

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 좋아요를 누른 사용자

    // 생성자
    public ReviewReplyLikes(ReviewReply reviewReply, User user) {
        this.reviewReply = reviewReply;
        this.user = user;
    }
}
