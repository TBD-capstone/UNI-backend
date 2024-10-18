package uni.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import uni.backend.controller.UserForm;

import java.time.LocalDateTime;

@Table(name = "user")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;  // ENUM 타입

    public static User createUser(UserForm userForm, PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setEmail(userForm.getEmail());
        user.setPassword(passwordEncoder.encode(userForm.getPassword()));
        user.setName(userForm.getName());
        user.setStatus("INACTIVE");
        user.setRole(userForm.getRole());

        return user;
    }
}