package uni.backend.service;

import uni.backend.domain.User;

import java.util.List;

public interface UserService {
    User saveUser(User user);
    void validateDuplicateUser(User user);
    List<User> findAllUsers(); // 회원 목록 조회 메서드
    List<User> findKoreanUsers(); // 한국 대학생만 조회
    User findByEmail(String email); // 이메일로 유저 조회
}