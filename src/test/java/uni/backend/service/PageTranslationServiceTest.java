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
import uni.backend.domain.dto.QnaResponse;
import uni.backend.domain.dto.ReplyResponse;
import uni.backend.repository.QnaRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional // 테스트 중 데이터 변경 사항을 롤백
public class PageTranslationServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PageTranslationServiceTest.class);
    @Autowired
    private PageTranslationService pageTranslationService; // 테스트할 서비스

    private List<QnaResponse> testData;

    @BeforeEach
    public void setUp() {
        // 테스트용 데이터 생성
        testData = createTestData();
    }

    @Test
    public void Qna_번역() {
        // Given
        String language = "en"; // 프랑스어로 번역
        pageTranslationService.translateQna(testData, language);

        // Then
        assertThat(testData.getFirst().getContent()).isNotNull(); // 번역 결과 확인
        assertThat(testData.size()).isEqualTo(2);
        for (QnaResponse qna : testData) {
            log.info(qna.getContent());
            for (ReplyResponse reply : qna.getReplies()) {
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
}
