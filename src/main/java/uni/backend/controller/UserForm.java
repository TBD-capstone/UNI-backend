package uni.backend.controller;

import lombok.Getter;
import lombok.Setter;
import uni.backend.domain.Role;

@Getter
@Setter
public class UserForm {
    private String name;
    private String email;
    private String password;
    private Role role;
}
