package uni.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class University {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "university_id")
  private Integer universityId;

  @Column(nullable = false, length = 255)
  private String uniName;

  public University(Integer universityId, String uniName) {
    this.universityId = universityId;
    this.uniName = uniName;
  }

  public University() {

  }

}
