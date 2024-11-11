package uni.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;
import uni.backend.domain.dto.SignupRequest;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {

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

}