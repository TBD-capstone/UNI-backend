package uni.backend.service;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import uni.backend.domain.Review;
import uni.backend.domain.ReviewReply;
import uni.backend.domain.dto.QnaResponse;
import uni.backend.domain.dto.ReplyResponse;
import uni.backend.domain.dto.ReviewReplyResponse;
import uni.backend.domain.dto.ReviewResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional // 테스트 중 데이터 변경 사항을 롤백
public class PageTranslationServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PageTranslationServiceTest.class);
    @Autowired
    private PageTranslationService pageTranslationService; // 테스트할 서비스

    private List<QnaResponse> qnaTestData;
    private List<ReviewResponse> reviewTestData;

    @Test
    public void Qna_번역() {
        // Given
        qnaTestData = createTestData();
        String language = "en"; // 프랑스어로 번역
        pageTranslationService.translateQna(qnaTestData, language);

        // Then
        assertThat(qnaTestData.getFirst().getContent()).isNotNull(); // 번역 결과 확인
        assertThat(qnaTestData.size()).isEqualTo(2);
        for (QnaResponse qna : qnaTestData) {
            log.info(qna.getContent());
            for (ReplyResponse reply : qna.getReplies()) {
                log.info(reply.getContent());
            }
        }

    }

    @Test
    public void Review_번역() {
        // Given
        reviewTestData = createReviewTestData();
        String language = "en"; // 프랑스어로 번역
        pageTranslationService.translateReview(reviewTestData, language);

        // Then
        assertThat(reviewTestData.getFirst().getContent()).isNotNull(); // 번역 결과 확인
        assertThat(reviewTestData.size()).isEqualTo(2);
        for (ReviewResponse review : reviewTestData) {
            log.info(review.getContent());
            for (ReviewReplyResponse reply : review.getReplies()) {
                log.info(reply.getContent());
            }
        }

    }

    private List<QnaResponse> createTestData() {
        ReplyResponse reply1 = new ReplyResponse(
            101231, 10199882, "Commenter A", "첫번째 질문에 대한 1번째 대답입니다.", 1,
            "profileA.png", false, null, 5L
        );
        ReplyResponse reply2 = new ReplyResponse(
            201231, 10299882, "Commenter B", "첫번째 질문에 대한 2번째 대답입니다.", 1,
            "profileB.png", false, null, 10L
        );
        QnaResponse qnaResponse1 = new QnaResponse(
            1, null, null, "첫번째 질문을 누군가가 남겼다!",
            new ArrayList<>(List.of(reply1, reply2)), "profileQna1.png",
            false, null, 15L
        );

        // 두 번째 QnaResponse와 관련된 ReplyResponse 생성
        ReplyResponse reply3 = new ReplyResponse(
            3152392, 103621920, "Commenter C", "매우 딱한 이야기로군요.. 1번째", 2,
            "profileC.png", false, null, 7L
        );
        ReplyResponse reply4 = new ReplyResponse(
            4152392, 104621920, "Commenter D", "매우 딱한 이야기로군요.. 2번째", 2,
            "profileD.png", false, null, 12L
        );
        ReplyResponse reply5 = new ReplyResponse(
            5152392, 105621920, "Commenter E", "매우 딱한 이야기로군요.. 3번째", 2,
            "profileE.png", false, null, 20L
        );
        QnaResponse qnaResponse2 = new QnaResponse(
            2, null, null, "면접준비를 해야하는데 너무 바빠요. 어떻게 하는 것이 좋을까요?",
            new ArrayList<>(List.of(reply3, reply4, reply5)), "profileQna2.png",
            false, null, 25L
        );

        // QnaResponse 리스트 반환
        return new ArrayList<>(List.of(qnaResponse1, qnaResponse2));
    }

    private List<ReviewResponse> createReviewTestData() {
        // 첫 번째 Review와 관련된 ReviewReply 생성
        ReviewReplyResponse reply1 = ReviewReplyResponse.builder()
            .replyId(1)
            .reviewId(1)
            .commenterId(2)
            .commenterName("ReplyUser1")
            .commenterImgProf("replyUser1.png")
            .content("This is reply 1 for review 1.")
            .likes(10L)
            .deleted(false)
            .build();

        ReviewReplyResponse reply2 = ReviewReplyResponse.builder()
            .replyId(2)
            .reviewId(1)
            .commenterId(3)
            .commenterName("ReplyUser2")
            .commenterImgProf("replyUser2.png")
            .content("This is reply 2 for review 1.")
            .likes(5L)
            .deleted(false)
            .build();

        // 첫 번째 Review 생성
        ReviewResponse review1 = ReviewResponse.builder()
            .matchingId(101)
            .reviewId(1)
            .profileOwnerId(1)
            .profileOwnerName("ProfileOwner1")
            .commenterId(2)
            .commenterName("Commenter1")
            .commenterImgProf("commenter1.png")
            .content("This is the first review.")
            .star(5)
            .likes(20L)
            .deleted(false)
            .replies(List.of(reply1, reply2))
            .build();

        // 두 번째 Review와 관련된 ReviewReply 생성
        ReviewReplyResponse reply3 = ReviewReplyResponse.builder()
            .replyId(3)
            .reviewId(2)
            .commenterId(4)
            .commenterName("ReplyUser3")
            .commenterImgProf("replyUser3.png")
            .content("This is reply 1 for review 2.")
            .likes(7L)
            .deleted(false)
            .build();

        ReviewReplyResponse reply4 = ReviewReplyResponse.builder()
            .replyId(4)
            .reviewId(2)
            .commenterId(5)
            .commenterName("ReplyUser4")
            .commenterImgProf("replyUser4.png")
            .content("This is reply 2 for review 2.")
            .likes(3L)
            .deleted(false)
            .build();

        ReviewReplyResponse reply5 = ReviewReplyResponse.builder()
            .replyId(5)
            .reviewId(2)
            .commenterId(6)
            .commenterName("ReplyUser5")
            .commenterImgProf("replyUser5.png")
            .content("This is reply 3 for review 2.")
            .likes(12L)
            .deleted(false)
            .build();

        // 두 번째 Review 생성
        ReviewResponse review2 = ReviewResponse.builder()
            .matchingId(102)
            .reviewId(2)
            .profileOwnerId(2)
            .profileOwnerName("ProfileOwner2")
            .commenterId(3)
            .commenterName("Commenter2")
            .commenterImgProf("commenter2.png")
            .content("This is the second review.")
            .star(4)
            .likes(15L)
            .deleted(false)
            .replies(List.of(reply3, reply4, reply5))
            .build();

        // ReviewResponse 리스트 반환
        return List.of(review1, review2);
    }


}
