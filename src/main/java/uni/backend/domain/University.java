package uni.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class University {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "university_id")
    private Integer universityId;

    @Column(nullable = false, length = 255)
    private String uniName;

    @Column
    private String zhUniName;

    @Column
    private String enUniName;

    public University(Integer universityId, String uniName) {
        this.universityId = universityId;
        this.uniName = uniName;
    }

    public University(Integer universityId, String uniName, String enUniName) {
        this.universityId = universityId;
        this.uniName = uniName;
        this.enUniName = enUniName;
    }

}
