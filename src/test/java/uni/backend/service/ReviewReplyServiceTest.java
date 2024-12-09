package uni.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uni.backend.domain.Matching;
import uni.backend.domain.Profile;
import uni.backend.domain.Review;
import uni.backend.domain.ReviewReply;
import uni.backend.domain.ReviewReplyLikes;
import uni.backend.domain.User;
import uni.backend.domain.dto.ReviewReplyResponse;
import uni.backend.repository.ReviewReplyLikeRepository;
import uni.backend.repository.ReviewReplyRepository;
import uni.backend.repository.ReviewRepository;
import uni.backend.repository.UserRepository;

class ReviewReplyServiceTest {

    @InjectMocks
    private ReviewReplyService reviewReplyService;

    @Mock
    private ReviewReplyRepository reviewReplyRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewReplyLikeRepository reviewReplyLikeRepository; // Mock 추가

    private Review review;
    private User profileOwner;
    private User commenter;
    private Matching matching;
    private ReviewReply reviewReply;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Profile profile = Profile.builder()
            .imgProf("example-image-url")
            .build();

        profileOwner = User.builder()
            .userId(1)
            .name("Profile Owner")
            .email("owner@example.com")
            .password("password")
            .profile(profile)
            .build();

        commenter = User.builder()
            .userId(2)
            .name("Commenter")
            .email("commenter@example.com")
            .password("password")
            .profile(profile)
            .build();

        review = Review.builder()
            .reviewId(1)
            .profileOwner(profileOwner)
            .commenter(commenter)
            .content("Initial review content")
            .likes(1L)
            .deleted(false)
            .build();

        reviewReply = ReviewReply.builder()
            .replyId(1)
            .review(review)
            .commenter(commenter)
            .content("Test reply")
            .likes(0L)
            .deleted(false)
            .build();
    }


    @Test
    @DisplayName("대댓글 작성 성공 테스트")
    void createReviewReply_성공() {
        // given
        when(reviewRepository.findById(review.getReviewId())).thenReturn(Optional.of(review));
        when(userRepository.findById(commenter.getUserId())).thenReturn(Optional.of(commenter));
        when(reviewReplyRepository.save(any(ReviewReply.class))).thenAnswer(
            invocation -> invocation.getArgument(0));

        // when
        ReviewReplyResponse createdReply = reviewReplyService.createReviewReply(
            review.getReviewId(),
            commenter.getUserId(),
            "New reply content"
        );

        // then
        assertNotNull(createdReply);
        assertEquals("New reply content", createdReply.getContent());
        assertEquals(review.getReviewId(), createdReply.getReviewId());
        assertEquals(commenter.getUserId(), createdReply.getCommenterId());
        assertEquals(0L, createdReply.getLikes()); // 좋아요 초기값 확인
        assertFalse(createdReply.getDeleted()); // 삭제 여부 확인

        // 검증: 저장 메서드 호출 여부 확인
        verify(reviewReplyRepository).save(any(ReviewReply.class));
    }


    @Test
    @DisplayName("대댓글 삭제 성공 테스트")
    void deleteReply_성공() {
        // given
        when(reviewReplyRepository.findById(reviewReply.getReplyId())).thenReturn(
            Optional.of(reviewReply));
        when(reviewReplyRepository.save(any(ReviewReply.class))).thenAnswer(
            invocation -> invocation.getArgument(0));

        // when
        ReviewReply deletedReply = reviewReplyService.deleteReply(reviewReply.getReplyId());

        // then
        assertNotNull(deletedReply);
        assertTrue(deletedReply.getDeleted());
        assertNotNull(deletedReply.getDeletedTime());

        verify(reviewReplyRepository).save(deletedReply);
    }

    @Test
    @DisplayName("대댓글 수정 성공 테스트")
    void updateReplyContent_성공() {
        // given
        String updatedContent = "Updated content";
        when(reviewReplyRepository.findById(reviewReply.getReplyId())).thenReturn(
            Optional.of(reviewReply));

        // when
        reviewReplyService.updateReplyContent(reviewReply.getReplyId(), updatedContent);

        // then
        assertEquals(updatedContent, reviewReply.getContent());
        assertNotNull(reviewReply.getUpdatedTime());

        // 검증: 업데이트된 객체가 저장되었는지 확인
        verify(reviewReplyRepository).save(reviewReply);
    }

    @Test
    @DisplayName("좋아요 추가 테스트")
    void toggleLike_좋아요_추가() {
        // given
        // 좋아요가 없는 상태로 설정
        when(reviewReplyRepository.findById(reviewReply.getReplyId())).thenReturn(
            Optional.of(reviewReply));
        when(userRepository.findById(commenter.getUserId())).thenReturn(Optional.of(commenter));
        when(reviewReplyLikeRepository.findByUserAndReviewReply(commenter, reviewReply)).thenReturn(
            Optional.empty());

        // when
        reviewReplyService.toggleLike(reviewReply.getReplyId(), commenter);

        // then
        assertEquals(1L, reviewReply.getLikes()); // 좋아요 수가 1로 증가했는지 확인
        verify(reviewReplyLikeRepository).save(any()); // 좋아요 저장 호출 확인
        verify(reviewReplyRepository).save(reviewReply); // 업데이트된 ReviewReply 저장 호출 확인
    }

    @Test
    @DisplayName("좋아요 취소 테스트")
    void toggleLike_좋아요_취소() {
        // given
        // 좋아요가 이미 눌린 상태로 설정
        ReviewReplyLikes existingLike = ReviewReplyLikes.builder()
            .user(commenter)
            .reviewReply(reviewReply)
            .build();
        reviewReply.increaseLikes(); // 좋아요 수를 1로 설정

        when(reviewReplyRepository.findById(reviewReply.getReplyId())).thenReturn(
            Optional.of(reviewReply));
        when(userRepository.findById(commenter.getUserId())).thenReturn(Optional.of(commenter));
        when(reviewReplyLikeRepository.findByUserAndReviewReply(commenter, reviewReply)).thenReturn(
            Optional.of(existingLike));

        // when
        reviewReplyService.toggleLike(reviewReply.getReplyId(), commenter);

        // then
        assertEquals(0L, reviewReply.getLikes()); // 좋아요 수가 0으로 감소했는지 확인
        verify(reviewReplyLikeRepository).delete(existingLike); // 좋아요 삭제 호출 확인
        verify(reviewReplyRepository).save(reviewReply); // 업데이트된 ReviewReply 저장 호출 확인
    }

    @Test
    @DisplayName("대댓글 작성 실패 - 리뷰 없음")
    void createReviewReply_리뷰없음() {
        when(reviewRepository.findById(review.getReviewId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewReplyService.createReviewReply(review.getReviewId(), commenter.getUserId(),
                "Test content");
        });

        assertEquals("리뷰를 찾을 수 없습니다. ID: " + review.getReviewId(), exception.getMessage());
        verify(reviewReplyRepository, never()).save(any());
    }

    @Test
    @DisplayName("대댓글 작성 실패 - 작성자 없음")
    void createReviewReply_작성자없음() {
        when(reviewRepository.findById(review.getReviewId())).thenReturn(Optional.of(review));
        when(userRepository.findById(commenter.getUserId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewReplyService.createReviewReply(review.getReviewId(), commenter.getUserId(),
                "Test content");
        });

        assertEquals("작성자를 찾을 수 없습니다. ID: " + commenter.getUserId(), exception.getMessage());
        verify(reviewReplyRepository, never()).save(any());
    }

    @Test
    @DisplayName("대댓글 수정 실패 - 대댓글 없음")
    void updateReplyContent_대댓글없음() {
        when(reviewReplyRepository.findById(reviewReply.getReplyId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewReplyService.updateReplyContent(reviewReply.getReplyId(), "Updated content");
        });

        assertEquals("대댓글을 찾을 수 없습니다. ID: " + reviewReply.getReplyId(), exception.getMessage());
        verify(reviewReplyRepository, never()).save(any());
    }

    @Test
    @DisplayName("대댓글 수정 실패 - 삭제된 대댓글")
    void updateReplyContent_삭제된대댓글() {
        reviewReply.softDelete(); // 대댓글 삭제 상태로 설정
        when(reviewReplyRepository.findById(reviewReply.getReplyId())).thenReturn(
            Optional.of(reviewReply));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            reviewReplyService.updateReplyContent(reviewReply.getReplyId(), "Updated content");
        });

        assertEquals("삭제된 대댓글은 수정할 수 없습니다.", exception.getMessage());
        verify(reviewReplyRepository, never()).save(any());
    }

    @Test
    @DisplayName("좋아요 토글 실패 - 대댓글 없음")
    void toggleLike_대댓글없음() {
        when(reviewReplyRepository.findById(reviewReply.getReplyId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewReplyService.toggleLike(reviewReply.getReplyId(), commenter);
        });

        assertEquals("대댓글을 찾을 수 없습니다. ID: " + reviewReply.getReplyId(), exception.getMessage());
        verify(reviewReplyLikeRepository, never()).save(any());
    }


}
