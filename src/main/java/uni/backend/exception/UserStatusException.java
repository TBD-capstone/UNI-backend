package uni.backend.exception;

import org.springframework.security.core.AuthenticationException;

public class UserStatusException extends AuthenticationException {

    public UserStatusException(String message) {
        super(message);
    }
}