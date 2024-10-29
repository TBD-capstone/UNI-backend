// package uni.backend.service;

// import org.assertj.core.api.Assertions;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.annotation.Rollback;
// import org.springframework.transaction.annotation.Transactional;
// import uni.backend.domain.Profile;
// import uni.backend.domain.Role;
// import uni.backend.domain.User;
// import uni.backend.repository.UserRepository;

// @SpringBootTest
// class UserServiceTest {

//     @Autowired
//     private UserRepository userRepository;
//     @Autowired
//     private UserServiceImpl userServiceImpl = new UserServiceImpl(userRepository);

//     @Test
//     @Rollback(value = false)
//     @Transactional
//     void 회원가입() {
//         User user = new User();

//         user.setName("test");
//         user.setEmail("test@test.com");
//         user.setPassword("123456");
//         user.setRole(Role.KOREAN);
//         user.setStatus("INACTIVE");

// //        userServiceImpl.signUp(user);
// //
// //        User findUser = userServiceImpl.findUser(user.getUserId());
// //        Assertions.assertThat(user.getUserId()).isEqualTo(findUser.getUserId());
//     }

// }
