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

import uni.backend.domain.*;
import uni.backend.domain.dto.ReviewReplyResponse;
import uni.backend.domain.dto.ReviewResponse;
import uni.backend.repository.*;

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

        Profile ownerProfile = new Profile();
        ownerProfile.setImgProf("owner-profile.jpg");

        profileOwner = User.builder()
            .userId(1)
            .name("Profile Owner")
            .profile(ownerProfile)
            .build();

        Profile commenterProfile = new Profile();
        commenterProfile.setImgProf("commenter-profile.jpg");

        commenter = User.builder()
            .userId(2)
            .name("Commenter")
            .profile(commenterProfile)
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


    @Test
    void 리뷰_생성_성공() {
        // given
        when(matchingRepository.findById(matching.getMatchingId())).thenReturn(
            Optional.of(matching));
        when(userRepository.findById(profileOwner.getUserId())).thenReturn(
            Optional.of(profileOwner));
        when(userRepository.findById(commenter.getUserId())).thenReturn(Optional.of(commenter));
        when(reviewRepository.save(any(Review.class))).thenAnswer(
            invocation -> invocation.getArgument(0));

        // when
        ReviewResponse createdReview = reviewService.createReview(
            matching.getMatchingId(),
            profileOwner.getUserId(),
            commenter.getUserId(),
            "Great experience!",
            5
        );

        // then
        assertNotNull(createdReview);
        assertEquals("Great experience!", createdReview.getContent());
        assertEquals(5, createdReview.getStar());
        verify(matchingRepository).findById(matching.getMatchingId());
        verify(userRepository, times(2)).findById(any());
        verify(reviewRepository).save(any(Review.class));
        verify(profileService).updateProfileStar(profileOwner.getUserId());
    }

    @Test
    void 리뷰_삭제_성공() {
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
    void 리뷰_업데이트_성공() {
        // given
        String updatedContent = "Updated review content";
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
        verify(reviewRepository).save(any(Review.class));
        verify(profileService).updateProfileStar(profileOwner.getUserId());
    }

    @Test
    void 좋아요_추가_성공() {
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
    void 좋아요_취소_성공() {
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
    void 대댓글이_null일_때_빈_리스트_반환() {
        // given
        when(reviewRepository.findById(review.getReviewId())).thenReturn(Optional.of(review));
        review.setReplies(null); // 대댓글이 없도록 설정

        // when
        List<ReviewReplyResponse> replies = reviewService.getReplyResponses(review);

        // then
        assertNotNull(replies);
        assertTrue(replies.isEmpty(), "대댓글이 없으면 빈 리스트가 반환되어야 합니다.");
    }

    
    @Test
    void 대댓글이_정상일_경우_내용_반환() {
        // given
        ReviewReply reply = new ReviewReply(review, commenter, "Test reply content");
        review.setReplies(List.of(reply)); // 정상 대댓글 설정

        // when
        List<ReviewReplyResponse> replies = reviewService.getReplyResponses(review);

        // then
        assertEquals("Test reply content", replies.get(0).getContent(),
            "정상 대댓글은 내용이 정상적으로 반환되어야 합니다.");
        assertNull(replies.get(0).getDeleteMessage(), "정상 대댓글은 deleteMessage가 null이어야 합니다.");
    }

    @Test
    void 리뷰_리스트_조회_성공() {
        // given
        when(reviewRepository.findByProfileOwnerUserId(profileOwner.getUserId())).thenReturn(
            List.of(review));

        // when
        List<ReviewResponse> reviews = reviewService.getReviewResponsesByUserId(
            profileOwner.getUserId());

        // then
        assertNotNull(reviews);
        assertEquals(1, reviews.size(), "리뷰 리스트의 크기는 1이어야 합니다.");
        assertEquals("Initial review content", reviews.get(0).getContent(),
            "리뷰 내용이 올바르게 반환되어야 합니다.");
    }

}
