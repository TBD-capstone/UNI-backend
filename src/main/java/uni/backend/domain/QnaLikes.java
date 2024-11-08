package uni.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Setter
@Getter
public class QnaLikes {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "qna_id", nullable = false)
  private Qna qna;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
}
