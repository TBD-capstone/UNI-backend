package uni.backend.domain;

import jakarta.persistence.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
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

  @Column(nullable = false, length = 255)
  private String status;  // VARCHAR(255)

  @Column(name = "univ_name")
  private String univName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;  // ENUM 타입

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
    return true;
  }

  public static User createUser(SignupRequest signupRequest, PasswordEncoder passwordEncoder) {
    User user = new User();
    user.setEmail(signupRequest.getEmail());
    user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
    user.setName(signupRequest.getName());
    user.setUnivName(signupRequest.getUnivName());
    user.setStatus("INACTIVE");
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