package uni.backend.controller;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import uni.backend.domain.Review;
import uni.backend.domain.User;
import uni.backend.domain.dto.*;
import uni.backend.service.PageTranslationService;
import uni.backend.service.ReviewService;
import uni.backend.service.UserServiceImpl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private PageTranslationService pageTranslationService;

    @InjectMocks
    private ReviewController reviewController;

    private ReviewCreateRequest reviewCreateRequest;
    private ReviewResponse reviewResponse;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize mock data using the Builder pattern
        reviewCreateRequest = ReviewCreateRequest.builder()
            .content("Sample review content")
            .star(4)
            .build();

        reviewResponse = ReviewResponse.builder()
            .reviewId(1)
            .matchingId(1)
            .profileOwnerId(1)
            .profileOwnerName("Owner")
            .commenterId(2)
            .commenterName("Commenter")
            .commenterImgProf("profileImageUrl")
            .content("Sample review content")
            .star(4)
            .likes(0L)
            .deleted(false)
            .build();

        user = mock(User.class); // Mock user for the tests
    }

    @Test
    @DisplayName("리뷰 생성 테스트")
    void testCreateReview() {
        // given
        Integer userId = 1, commenterId = 2, matchingId = 3;

        when(reviewService.createReview(matchingId, userId, commenterId,
            reviewCreateRequest.getContent(), reviewCreateRequest.getStar()))
            .thenReturn(reviewResponse);

        // when
        ResponseEntity<ReviewCreateResponse> response = reviewController.createReview(userId,
            commenterId, matchingId, reviewCreateRequest);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Review가 성공적으로 작성되었습니다.", response.getBody().getMessage());
        assertEquals(reviewResponse, response.getBody().getReview());
        verify(reviewService, times(1)).createReview(matchingId, userId, commenterId,
            reviewCreateRequest.getContent(), reviewCreateRequest.getStar());
    }

    @Test
    @DisplayName("특정 유저의 리뷰 목록 조회")
    void testGetReviewsByUserId() {
        // given
        Integer userId = 1;
        List<ReviewResponse> reviewResponses = List.of(reviewResponse);

        when(reviewService.getReviewResponsesByUserId(userId)).thenReturn(reviewResponses);

        // when
        ResponseEntity<List<ReviewResponse>> response = reviewController.getReviewsByUserId(userId,
            "en");

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(reviewResponses, response.getBody());
        verify(reviewService, times(1)).getReviewResponsesByUserId(userId);
    }

    @Test
    @DisplayName("리뷰 좋아요 토글")
    void testToggleLike() {
        // given
        Integer reviewId = 1, userId = 2;
        Review updatedReview = new Review();
        updatedReview.setLikes(1L);

        when(userService.findById(userId)).thenReturn(user);
        when(reviewService.toggleLike(reviewId, user)).thenReturn(updatedReview);

        // when
        ResponseEntity<Response> response = reviewController.toggleLike(reviewId, userId);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("좋아요 상태가 변경되었습니다. 현재 좋아요 수: 1", response.getBody().getMessage());
        verify(reviewService, times(1)).toggleLike(reviewId, user);
    }

    @Test
    @DisplayName("리뷰 수정")
    void testUpdateReview() {
        // given
        Integer reviewId = 1;
        ReviewCreateRequest updatedReviewRequest = ReviewCreateRequest.builder()
            .content("Updated content")
            .star(5)
            .build();

        // when
        ResponseEntity<Response> response = reviewController.updateReview(reviewId,
            updatedReviewRequest);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Review가 성공적으로 수정되었습니다.", response.getBody().getMessage());
        verify(reviewService, times(1)).updateReviewContent(reviewId,
            updatedReviewRequest.getContent(), updatedReviewRequest.getStar());
    }

    @Test
    @DisplayName("리뷰 삭제")
    void testDeleteReview() {
        // given
        Integer reviewId = 1;

        // when
        ResponseEntity<Response> response = reviewController.deleteReview(reviewId);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Review가 삭제되었습니다.", response.getBody().getMessage());
        verify(reviewService, times(1)).deleteReview(reviewId);
    }
}