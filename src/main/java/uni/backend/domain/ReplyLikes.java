package uni.backend.domain;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class ReplyLikes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reply_id", nullable = false)
    private Reply reply; // 좋아요가 눌린 대댓글

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 좋아요를 누른 사용자

}
