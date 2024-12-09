package uni.backend.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReviewTest {

    private Review review;

    @BeforeEach
    void setUp() {
        review = Review.builder()
            .content("Great review!")
            .star(5)
            .likes(0L)
            .isBlind(false)
            .deleted(false)
            .build();
    }

    @Test
    void getBlindReview_ShouldReturnContent_WhenNotBlind() {
        // Arrange
        review.setIsBlind(false);

        // Act
        String result = review.getBlindReview();

        // Assert
        assertEquals("Great review!", result);
    }

    @Test
    void getBlindReview_ShouldReturnBlindMessage_WhenBlind() {
        // Arrange
        review.setIsBlind(true);

        // Act
        String result = review.getBlindReview();

        // Assert
        assertEquals("이 리뷰는 블라인드 처리되었습니다.", result);
    }

    @Test
    void increaseLikes_ShouldIncrementLikes() {
        // Arrange
        long initialLikes = review.getLikes();

        // Act
        review.increaseLikes();

        // Assert
        assertEquals(initialLikes + 1, review.getLikes());
    }

    @Test
    void decreaseLikes_ShouldDecrementLikes() {
        // Arrange
        review.setLikes(5L);
        long initialLikes = review.getLikes();

        // Act
        review.decreaseLikes();

        // Assert
        assertEquals(initialLikes - 1, review.getLikes());
    }

    @Test
    void delete_ShouldMarkAsDeletedAndSetDeletedTime() {
        // Act
        review.delete();

        // Assert
        assertTrue(review.getDeleted());
        assertNotNull(review.getDeletedTime());
    }

    @Test
    void updateContent_ShouldChangeContentAndUpdateTime() {
        // Arrange
        String newContent = "Updated review content.";

        // Act
        review.updateContent(newContent);

        // Assert
        assertEquals(newContent, review.getContent());
        assertNotNull(review.getUpdatedTime());
    }

    @Test
    void blindReview_ShouldMarkAsBlind() {
        // Act
        review.blindReview();

        // Assert
        assertTrue(review.getIsBlind());
    }

    @Test
    void unblindReview_ShouldUnmarkAsBlind() {
        // Arrange
        review.setIsBlind(true);

        // Act
        review.unblindReview();

        // Assert
        assertFalse(review.getIsBlind());
    }
}
