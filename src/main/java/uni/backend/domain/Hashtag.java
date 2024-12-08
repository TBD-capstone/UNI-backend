package uni.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hashtag_id")
    private Integer hashtagId;

    @Column(nullable = false)
    private String hashtagName;

    @Column
    private String entagName; // 영어 해시태그

    @Column
    private String zhtagName; // 중국어 해시태그

    @OneToMany(mappedBy = "hashtag", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MainCategory> mainCategories;
}
