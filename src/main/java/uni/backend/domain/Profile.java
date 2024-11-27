package uni.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "profile_id")
  private Integer profileId;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = true) // null 허용
  private User user;

  @Column(nullable = true) // null 허용
  private LocalDateTime createdAt;

  @Column(nullable = true) // null 허용
  private LocalDateTime updatedAt;

  @Column(nullable = true) // null 허용
  private String imgProf;

  @Column(nullable = true) // null 허용
  private String imgBack;

  @Column(nullable = true) // null 허용
  private String region;

  @Column(nullable = true) // null 허용
  private String description;

  @Column(nullable = true) // null 허용
  private Integer numEmployment;

  @Column(nullable = true) // null 허용
  private Double star;

  @Column(nullable = true) // null 허용
  private String time;

  @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MainCategory> mainCategories = new ArrayList<>();

    @Column(nullable = false)
    private boolean isVisible = true;

    public void addMainCategory(MainCategory mainCategory) {
        this.mainCategories.add(mainCategory);
        mainCategory.setProfile(this);
    }

  public void removeMainCategory(MainCategory mainCategory) {
    mainCategories.remove(mainCategory);
    mainCategory.setProfile(null);
  }

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
    this.numEmployment = 0;
    this.star = 0.0;
  }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void hideProfile() {
        this.isVisible = false;
    }

    public void showProfile() {
        this.isVisible = true;
    }


    public List<String> getHashtagStringList() {
        List<String> hashtags;
        hashtags = this.mainCategories.stream()
            .map(mainCategory -> {
                Hashtag hashtag = mainCategory.getHashtag();
                return hashtag != null ? hashtag.getHashtagName() : null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return hashtags;
    }
}
