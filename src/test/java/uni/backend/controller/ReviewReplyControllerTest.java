package uni.backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uni.backend.domain.Profile;
import uni.backend.domain.Review;
import uni.backend.domain.ReviewReply;
import uni.backend.domain.User;
import uni.backend.domain.dto.Response;
import uni.backend.domain.dto.ReviewReplyCreateRequest;
import uni.backend.domain.dto.ReviewReplyCreateResponse;
import uni.backend.domain.dto.ReviewReplyResponse;
import uni.backend.service.ReviewReplyService;
import uni.backend.service.UserService;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReviewReplyControllerTest {

    @Mock
    private ReviewReplyService reviewReplyService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReviewReplyController reviewReplyController;

    @Test
    @DisplayName("대댓글 생성 테스트")
    void testCreateReply() {
        // given
        Integer reviewId = 1, commenterId = 2;
        String content = "대댓글 내용";

        // ReviewReplyResponse 객체 빌더 패턴을 사용하여 생성
        ReviewReplyResponse reviewReplyResponse = ReviewReplyResponse.builder()
            .replyId(1)
            .reviewId(reviewId)
            .commenterId(commenterId)
            .commenterName("commenter")
            .commenterImgProf("profileImg")
            .content(content)
            .likes(0L)
            .deleted(false)
            .build();

        // ReviewReplyCreateRequest 객체를 빌더 패턴을 사용하여 생성
        ReviewReplyCreateRequest request = ReviewReplyCreateRequest.builder()
            .content(content)
            .build();

        // when
        when(reviewReplyService.createReviewReply(reviewId, commenterId, content)).thenReturn(
            reviewReplyResponse);

        // when
        ResponseEntity<ReviewReplyCreateResponse> response = reviewReplyController.createReply(
            reviewId, commenterId, request);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("대댓글이 성공적으로 작성되었습니다.", response.getBody().getMessage());
        assertEquals(reviewReplyResponse, response.getBody().getReply());
        verify(reviewReplyService, times(1)).createReviewReply(reviewId, commenterId, content);
    }

    @Test
    @DisplayName("대댓글 삭제 테스트")
    void testDeleteReply() {
        // given
        Integer replyId = 1;

        // Create a mock ReviewReply object to return when deleteReply() is called
        ReviewReply reviewReply = ReviewReply.builder()
            .replyId(replyId)
            .content("대댓글 내용")
            .likes(0L)
            .deleted(false)
            .build();

        // Mock the behavior of deleteReply to return the mock ReviewReply object
        when(reviewReplyService.deleteReply(replyId)).thenReturn(reviewReply);

        // when
        ResponseEntity<Response> response = reviewReplyController.deleteReply(replyId);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("대댓글이 성공적으로 삭제되었습니다.", response.getBody().getMessage());
        verify(reviewReplyService, times(1)).deleteReply(replyId);
    }

    @Test
    @DisplayName("대댓글 수정 테스트")
    void testUpdateReplyContent() {
        // given
        Integer replyId = 1;
        String newContent = "수정된 대댓글 내용";

        // ReviewReplyCreateRequest 객체를 빌더 패턴을 사용하여 생성
        ReviewReplyCreateRequest request = ReviewReplyCreateRequest.builder()
            .content(newContent)
            .build();

        // 대댓글 수정 서비스 메서드 mock 설정
        doNothing().when(reviewReplyService).updateReplyContent(replyId, newContent);

        // when
        ResponseEntity<Response> response = reviewReplyController.updateReplyContent(replyId,
            request);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("대댓글이 성공적으로 수정되었습니다.", response.getBody().getMessage());
        verify(reviewReplyService, times(1)).updateReplyContent(replyId, newContent);
    }

    @Test
    @DisplayName("특정 대댓글 조회 테스트")
    void testGetReply() {
        // given
        Integer replyId = 1;

        // ReviewReplyResponse 객체 빌더 패턴을 사용하여 생성
        ReviewReply reviewReply = ReviewReply.builder()
            .replyId(replyId)
            .review(Review.builder().reviewId(1).build()) // 필요한 객체 초기화
            .commenter(User.builder().userId(2).name("commenter").profile(
                Profile.builder().imgProf("profileImg").build()).build())
            .content("대댓글 내용")
            .likes(0L)
            .deleted(false)
            .build();

        // ReviewReplyResponse 빌더 패턴으로 객체 생성
        ReviewReplyResponse reviewReplyResponse = ReviewReplyResponse.builder()
            .replyId(replyId)
            .reviewId(1)
            .commenterId(2)
            .commenterName("commenter")
            .commenterImgProf("profileImg")
            .content("대댓글 내용")
            .likes(0L)
            .deleted(false)
            .build();

        // reviewReplyService.getReply(replyId)가 reviewReply 객체를 반환하도록 설정
        when(reviewReplyService.getReply(replyId)).thenReturn(reviewReply);

        // when
        ResponseEntity<ReviewReplyResponse> response = reviewReplyController.getReply(replyId);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(reviewReplyResponse, response.getBody());
        verify(reviewReplyService, times(1)).getReply(replyId);
    }

    @Test
    @DisplayName("특정 리뷰의 모든 대댓글 조회 테스트")
    void testGetRepliesByReviewId() {
        // given
        Integer reviewId = 1;
        List<ReviewReply> reviewReplies = List.of(new ReviewReply());
        when(reviewReplyService.getRepliesByReviewId(reviewId)).thenReturn(reviewReplies);

        // when
        ResponseEntity<List<ReviewReply>> response = reviewReplyController.getRepliesByReviewId(
            reviewId);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(reviewReplies, response.getBody());
        verify(reviewReplyService, times(1)).getRepliesByReviewId(reviewId);
    }

    @Test
    @DisplayName("대댓글 좋아요 토글 테스트")
    void testToggleLike() {
        // given
        Integer replyId = 1, userId = 2;
        User user = mock(User.class);
        when(userService.findById(userId)).thenReturn(user);
        doNothing().when(reviewReplyService).toggleLike(replyId, user);

        // when
        ResponseEntity<Response> response = reviewReplyController.toggleLike(replyId, userId);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("좋아요 상태가 변경되었습니다.", response.getBody().getMessage());
        verify(reviewReplyService, times(1)).toggleLike(replyId, user);
    }
}