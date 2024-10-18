package uni.backend.service;

import uni.backend.domain.User;

public interface UserService {
    User saveUser(User user);
    void validateDuplicateUser(User user);
}