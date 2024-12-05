package uni.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uni.backend.domain.Matching;
import uni.backend.domain.Review;
import uni.backend.domain.ReviewLikes;
import uni.backend.domain.User;
import uni.backend.domain.dto.ReviewResponse;
import uni.backend.repository.MatchingRepository;
import uni.backend.repository.ReviewLikeRepository;
import uni.backend.repository.ReviewRepository;
import uni.backend.repository.UserRepository;

public class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ProfileService profileService;

    @Mock
    private MatchingRepository matchingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewLikeRepository reviewLikeRepository;

    private User profileOwner;
    private User commenter;
    private Matching matching;
    private Review review;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        profileOwner = User.builder()
            .userId(1)
            .name("Profile Owner")
            .email("owner@example.com")
            .password("password")
            .build();

        commenter = User.builder()
            .userId(2)
            .name("Commenter")
            .email("commenter@example.com")
            .password("password")
            .build();

        matching = Matching.builder()
            .matchingId(1)
            .requester(profileOwner)
            .receiver(commenter)
            .status(Matching.Status.ACCEPTED)
            .createdAt(LocalDateTime.now())
            .build();

        review = Review.builder()
            .reviewId(1)
            .matching(matching)
            .profileOwner(profileOwner)
            .commenter(commenter)
            .content("Initial review content")
            .star(5)
            .likes(1L)
            .deleted(false)
            .build();
    }

//    @Test
//    void createReview_성공() {
//        // given
//        when(matchingRepository.findById(matching.getMatchingId())).thenReturn(
//            Optional.of(matching));
//        when(userRepository.findById(profileOwner.getUserId())).thenReturn(
//            Optional.of(profileOwner));
//        when(userRepository.findById(commenter.getUserId())).thenReturn(Optional.of(commenter));
//        when(reviewRepository.save(any(Review.class))).thenAnswer(
//            invocation -> invocation.getArgument(0));
//
//        // when
//        ReviewResponse createdReview = reviewService.createReview(
//            matching.getMatchingId(),
//            profileOwner.getUserId(),
//            commenter.getUserId(),
//            "Great experience!",
//            5
//        );
//
//        // then
//        assertNotNull(createdReview);
//        assertEquals("Great experience!", createdReview.getContent());
//        assertEquals(5, createdReview.getStar());
//        verify(matchingRepository).findById(matching.getMatchingId());
//        verify(userRepository, times(2)).findById(any());
//        verify(reviewRepository).save(any(Review.class));
//        verify(profileService).updateProfileStar(profileOwner.getUserId());
//    }

    @Test
    void deleteReview_성공() {
        // given
        when(reviewRepository.findById(review.getReviewId())).thenReturn(Optional.of(review));

        // when
        reviewService.deleteReview(review.getReviewId());

        // then
        assertTrue(review.getDeleted());
        assertNotNull(review.getDeletedTime());
        verify(reviewRepository).findById(review.getReviewId());
        verify(profileService).updateProfileStar(profileOwner.getUserId());
    }

    @Test
    void updateReviewContent_성공() {
        // given
        String updatedContent = "Updated content";
        int updatedStar = 4;

        when(reviewRepository.findById(review.getReviewId())).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // when
        Review updatedReview = reviewService.updateReviewContent(review.getReviewId(),
            updatedContent, updatedStar);

        // then
        assertEquals(updatedContent, updatedReview.getContent());
        assertEquals(updatedStar, updatedReview.getStar());
        verify(reviewRepository).findById(review.getReviewId());
        verify(reviewRepository).save(review);
        verify(profileService).updateProfileStar(profileOwner.getUserId());
    }

    @Test
    void toggleLike_좋아요_추가() {
        // given
        when(reviewRepository.findById(review.getReviewId())).thenReturn(Optional.of(review));
        when(reviewLikeRepository.findByUserAndReview(commenter, review)).thenReturn(
            Optional.empty());

        // when
        reviewService.toggleLike(review.getReviewId(), commenter);

        // then
        assertEquals(2L, review.getLikes());
        verify(reviewLikeRepository).save(any(ReviewLikes.class));
        verify(reviewRepository).save(review);
    }

    @Test
    void toggleLike_좋아요_취소() {
        // given
        ReviewLikes like = new ReviewLikes();
        like.setUser(commenter);
        like.setReview(review);

        when(reviewRepository.findById(review.getReviewId())).thenReturn(Optional.of(review));
        when(reviewLikeRepository.findByUserAndReview(commenter, review)).thenReturn(
            Optional.of(like));

        // when
        reviewService.toggleLike(review.getReviewId(), commenter);

        // then
        assertEquals(0L, review.getLikes());
        verify(reviewLikeRepository).delete(like);
        verify(reviewRepository).save(review);
    }

    @Test
    void getReviewResponsesByUserId_성공() {
        // given
        when(reviewRepository.findByProfileOwnerUserId(profileOwner.getUserId())).thenReturn(
            List.of(review));

        // when
        List<Review> reviews = reviewService.getReviewsByUserId(profileOwner.getUserId());

        // then
        assertNotNull(reviews);
        assertEquals(1, reviews.size());
        verify(reviewRepository).findByProfileOwnerUserId(profileOwner.getUserId());
    }
}
