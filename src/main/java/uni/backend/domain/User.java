package uni.backend.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import uni.backend.domain.dto.SignupRequest;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Integer userId;  // INT 타입, Primary Key

  @Column(unique = true, nullable = false, length = 255)
  private String email;  // VARCHAR(255)

  @Column(nullable = false, length = 255)
  private String password;  // VARCHAR(255)

  @Column(nullable = false, length = 255)
  private String name;  // VARCHAR(255)

  @Column(name = "last_verification")
  private LocalDateTime lastVerification;  // DATETIME 타입

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;  // ENUM 타입으로 변경

  @Column(name = "univ_name")
  private String univName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;  // ENUM 타입

    @Column(name = "admin_id", unique = true, nullable = true)
    private String adminId;

    @Column(name = "end_ban_date")
    private LocalDateTime endBanDate; // 제재 해제일

    @Column(name = "report_count", nullable = false)
    private Long reportCount = 0L; // 신고 횟수

    @Column(name = "last_report_reason")
    private String lastReportReason; // 마지막 신고 사유

    @OneToMany(mappedBy = "reportedUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reportsAgainst = new ArrayList<>();

    @OneToMany(mappedBy = "reporterUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reportsMade = new ArrayList<>();


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public String getUsername() {
    return this.email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

    @Override
    public boolean isEnabled() {
        return this.status == UserStatus.ACTIVE && !isBanned();
    }

    public boolean isBanned() {
        return endBanDate != null && endBanDate.isAfter(LocalDateTime.now());
    }

    //특정 유저를 밴 상태로 설정
    public void banUser(LocalDateTime until, String reason) {
        this.status = UserStatus.BANNED;
        this.endBanDate = until;
        this.lastReportReason = reason;
    }

    public void incrementReportCount(String reason) {
        this.reportCount += 1;
        this.lastReportReason = reason;

        // 임시 정책 : 신고 횟수가 특정 값을 초과하면 자동으로 밴 처리 (예: 3회 이상)
        if (this.reportCount >= 3) {
            banUser(LocalDateTime.now().plusDays(7), reason); // 7일 밴 처리
        }
    }


    //특정 유저의 밴 상태를 해제.
    public void unbanUser() {
        this.status = UserStatus.ACTIVE;
        this.endBanDate = null;
        this.lastReportReason = null;
    }


    public static User createUser(SignupRequest signupRequest, PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setName(signupRequest.getName());
        user.setUnivName(signupRequest.getUnivName());
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(signupRequest.getIsKorean() ? Role.KOREAN : Role.EXCHANGE);

    return user;
  }

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Profile profile;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<MatchingJoin> matchingJoins;

  @OneToMany(mappedBy = "profileOwner", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Qna> qnas; // 사용자가 작성한 Qna 리스트

  @OneToMany(mappedBy = "commenter", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Qna> commentedQnas; // 사용자가 작성한 댓글 리스트

  @OneToMany(mappedBy = "commenter", cascade = CascadeType.ALL, orphanRemoval = true) // 변경된 부분
  private List<Reply> replies; // 사용자가 작성한 대댓글 리스트

}