package uni.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
public class MainCategory {

    @Id
    @ManyToOne
    @JoinColumn(name = "profileId")
    private Profile profile;

    @Id
    @ManyToOne
    @JoinColumn(name = "hashtagId")
    private Hashtag hashtag;
}
