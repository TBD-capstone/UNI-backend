// package uni.backend.service;

// import org.junit.jupiter.api.Assertions;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// import java.util.List;

// @SpringBootTest
// public class HashtagServiceTest {

//     @Autowired
//     private HashtagService hashtagService;

//     @Test
//     void 해시태그로_사용자_검색_존재() {
//         List<String> hashtags = List.of("java", "spring");
//         List<String> users = hashtagService.findUsersByHashtags(hashtags);


//         System.out.println(hashtags);
//         System.out.println(users);
//         Assertions.assertEquals(users.size(), 1);
// //        System.out.println(users);
//     }

//     @Test
//     void 해시태그로_사용자_검색_비존재1() {
//         List<String> hashtags = List.of("java", "tag1");
//         List<String> users = hashtagService.findUsersByHashtags(hashtags);

//         Assertions.assertEquals(users.size(), 0);
//     }

//     @Test
//     void 해시태그로_사용자_검색_비존재2() {
//         List<String> hashtags = List.of("java", "NonValidTag");
//         List<String> users = hashtagService.findUsersByHashtags(hashtags);

//         Assertions.assertEquals(users.size(), 0);
//     }
// }