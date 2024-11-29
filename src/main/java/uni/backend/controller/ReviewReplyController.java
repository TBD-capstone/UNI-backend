package uni.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.ReviewReply;
import uni.backend.domain.User;
import uni.backend.domain.dto.Response;
import uni.backend.domain.dto.ReviewReplyCreateRequest;
import uni.backend.domain.dto.ReviewReplyCreateResponse;
import uni.backend.domain.dto.ReviewReplyResponse;
import uni.backend.service.ReviewReplyService;
import uni.backend.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewReplyController {

    private final ReviewReplyService reviewReplyService;
    private final UserService userService;

    // **대댓글 작성**
    @PostMapping("/{reviewId}/reply/{commenterId}")
    public ResponseEntity<ReviewReplyCreateResponse> createReply(
        @PathVariable Integer reviewId,
        @PathVariable Integer commenterId,
        @RequestBody ReviewReplyCreateRequest request) {

        // 서비스에서 응답 생성
        ReviewReplyResponse response = reviewReplyService.createReviewReply(
            reviewId,
            commenterId,
            request.getContent()
        );

        return ResponseEntity.ok(
            ReviewReplyCreateResponse.success("대댓글이 성공적으로 작성되었습니다.", response));
    }

    // **대댓글 삭제**
    @DeleteMapping("/reply/{replyId}")
    public ResponseEntity<Response> deleteReply(@PathVariable Integer replyId) {
        reviewReplyService.deleteReply(replyId);
        return ResponseEntity.ok(Response.successMessage("대댓글이 성공적으로 삭제되었습니다."));
    }

    // **대댓글 수정**
    @PatchMapping("/reply/{replyId}")
    public ResponseEntity<Response> updateReplyContent(
        @PathVariable Integer replyId,
        @RequestBody ReviewReplyCreateRequest request) {

        reviewReplyService.updateReplyContent(replyId, request.getContent());
        return ResponseEntity.ok(Response.successMessage("대댓글이 성공적으로 수정되었습니다."));
    }


    // **특정 대댓글 조회**
    @GetMapping("/reply/{replyId}")
    public ResponseEntity<ReviewReplyResponse> getReply(@PathVariable Integer replyId) {
        ReviewReply reply = reviewReplyService.getReply(replyId);

        boolean isDeleted = Boolean.TRUE.equals(reply.getDeleted());
        ReviewReplyResponse response = ReviewReplyResponse.builder()
            .replyId(reply.getReplyId())
            .reviewId(reply.getReview().getReviewId())
            .commenterId(reply.getCommenter().getUserId())
            .commenterName(reply.getCommenter().getName())
            .commenterImgProf(reply.getCommenter().getProfile().getImgProf())
            .content(isDeleted ? null : reply.getContent())
            .deleted(reply.getDeleted())
            .deletedTime(reply.getDeletedTime())
            .updatedTime(reply.getUpdatedTime())
            .deleteMessage(isDeleted ? "삭제된 대댓글입니다." : null)
            .likes(reply.getLikes())
            .build();

        return ResponseEntity.ok(response);
    }


    // **특정 리뷰의 모든 대댓글 조회**
    @GetMapping("/{reviewId}/replies")
    public ResponseEntity<List<ReviewReply>> getRepliesByReviewId(@PathVariable Integer reviewId) {
        List<ReviewReply> replies = reviewReplyService.getRepliesByReviewId(reviewId);
        return ResponseEntity.ok(replies);
    }

    // **대댓글 좋아요 토글**
    @PostMapping("/reply/{replyId}/like/{userId}")
    public ResponseEntity<Response> toggleLike(
        @PathVariable Integer replyId, // PathVariable로 replyId 받음
        @PathVariable Integer userId  // PathVariable로 userId 받음
    ) {
        User user = userService.findById(userId); // userId로 User 엔티티 조회
        reviewReplyService.toggleLike(replyId, user); // 좋아요 토글 로직 실행

        return ResponseEntity.ok(Response.successMessage("좋아요 상태가 변경되었습니다."));
    }

}
