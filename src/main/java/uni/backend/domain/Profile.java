package uni.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Profile {

    @Id
    @GeneratedValue
    @Column(name = "profile_id")
    private Long id;

    @Column(length = 1000)
    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(length = 255)
    private String hashtag;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;




//
//    Attribute	Type	Description
//    profile_id	INT	프로필의 고유 ID (기본 키)
//    user_id	INT	프로필 소유자의 사용자 ID
//    created_at	DATETIME	프로필 생성 시간
//    updated_at	DATETIME	프로필 업데이트 시간
//    content	VARCHAR(1000)	프로필 내용
//    hashtag	VARCHAR(255)	관련 해시태그




}
