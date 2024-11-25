package uni.backend.controller;


import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.Review;
import uni.backend.domain.User;
import uni.backend.domain.dto.ReplyResponse;
import uni.backend.domain.dto.Response;
import uni.backend.domain.dto.ReviewCreateRequest;
import uni.backend.domain.dto.ReviewCreateResponse;
import uni.backend.domain.dto.ReviewReplyResponse;
import uni.backend.domain.dto.ReviewResponse;
import uni.backend.service.ReviewService;
import java.util.List;
import uni.backend.service.UserService;
import uni.backend.service.UserServiceImpl;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserServiceImpl userService;


    @PostMapping("/user/{userId}/review/{commenterId}/matching/{matchingId}")
    public ResponseEntity<ReviewCreateResponse> createReview(
        @PathVariable Integer userId,
        @PathVariable Integer commenterId,
        @PathVariable Integer matchingId, // 매칭 ID 추가
        @RequestBody ReviewCreateRequest request) {

        Review review = reviewService.createReview(
            matchingId,
            userId,
            commenterId,
            request.getContent(),
            request.getStar()
        );

        ReviewResponse response = ReviewResponse.builder()
            .reviewId(review.getReviewId())
            .matchingId(review.getMatching().getMatchingId()) // 매칭 ID 추가
            .profileOwnerId(review.getProfileOwner().getUserId())
            .profileOwnerName(review.getProfileOwner().getName())
            .commenterId(review.getCommenter().getUserId())
            .commenterName(review.getCommenter().getName())
            .commenterImgProf(review.getCommenter().getProfile().getImgProf())
            .content(review.getContent())
            .star(review.getStar())
            .likes(review.getLikes())
            .deleted(review.getDeleted())
            .deletedTime(review.getDeletedTime())
            .updatedTime(review.getUpdatedTime())
            .build();

        return ResponseEntity.ok(ReviewCreateResponse.success("Review가 성공적으로 작성되었습니다.", response));
    }

//    @GetMapping("/review/{userId}")
//    public ResponseEntity<List<ReviewResponse>> getReviewsByUserId(@PathVariable Integer userId) {
//        // 특정 유저의 리뷰 목록 조회
//        List<Review> reviews = reviewService.getReviewsByUserId(userId);
//
//        // Review -> ReviewResponse로 변환
//        List<ReviewResponse> response = reviews.stream()
//            .map(review -> {
//                boolean isDeleted = Boolean.TRUE.equals(review.getDeleted()); // 리뷰 삭제 여부 확인
//
//                // 대댓글 리스트 변환
//                List<ReviewReplyResponse> replyResponses = review.getReplies().stream()
//                    .map(reply -> {
//                        boolean isReplyDeleted = Boolean.TRUE.equals(
//                            reply.getDeleted()); // 대댓글 삭제 여부 확인
//                        return ReviewReplyResponse.builder()
//                            .replyId(reply.getReplyId())
//                            .reviewId(reply.getReview().getReviewId())
//                            .commenterId(reply.getCommenter().getUserId())
//                            .commenterName(reply.getCommenter().getName())
//                            .commenterImgProf(reply.getCommenter().getProfile().getImgProf())
//                            .content(
//                                isReplyDeleted ? null : reply.getContent()) // 삭제된 경우 content는 null
//                            .deleted(reply.getDeleted())
//                            .deletedTime(reply.getDeletedTime())
//                            .updatedTime(reply.getUpdatedTime())
//                            .deleteMessage(isReplyDeleted ? "삭제된 대댓글입니다." : null) // 삭제된 경우 메시지
//                            .likes(reply.getLikes())
//                            .build();
//                    })
//                    .collect(Collectors.toList());
//
//                return ReviewResponse.builder()
//                    .reviewId(review.getReviewId())
//                    .matchingId(review.getMatching() != null ? review.getMatching().getMatchingId()
//                        : null) // 매칭 ID 추가
//                    .content(isDeleted ? null : review.getContent()) // 삭제된 경우 content는 null
//                    .star(isDeleted ? null : review.getStar()) // 삭제된 경우 star는 null
//                    .likes(review.getLikes())
//                    .profileOwnerId(review.getProfileOwner().getUserId())
//                    .profileOwnerName(review.getProfileOwner().getName())
//                    .commenterId(review.getCommenter().getUserId())
//                    .commenterName(review.getCommenter().getName())
//                    .deleted(review.getDeleted())
//                    .deletedTime(review.getDeletedTime())
//                    .updatedTime(review.getUpdatedTime())
//                    .deleteMessage(isDeleted ? "삭제된 리뷰입니다." : null) // 삭제된 경우 메시지 추가
//                    .replies(replyResponses) // 대댓글 리스트 추가
//                    .build();
//            })
//            .collect(Collectors.toList());
//
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/review/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUserId(@PathVariable Integer userId) {
        List<ReviewResponse> responses = reviewService.getReviewResponsesByUserId(userId);
        return ResponseEntity.ok(responses);
    }


    @PostMapping("/review/{reviewId}/likes/{userId}")
    public ResponseEntity<Response> toggleLike(
        @PathVariable Integer reviewId,
        @PathVariable Integer userId) {

        // userId로 User 엔티티를 조회 (서비스 계층에서 처리)
        User user = userService.findById(userId);

        // ReviewService에서 좋아요 상태 변경
        Review updatedReview = reviewService.toggleLike(reviewId, user);

        // 성공 메시지 반환
        String message = "좋아요 상태가 변경되었습니다. 현재 좋아요 수: " + updatedReview.getLikes();
        return ResponseEntity.ok(Response.successMessage(message));
    }


    // 리뷰 수정
    @PatchMapping("/review/{reviewId}")
    public ResponseEntity<Response> updateReview(
        @PathVariable Integer reviewId,
        @RequestBody ReviewCreateRequest request) {

        reviewService.updateReviewContent(reviewId, request.getContent(), request.getStar());
        return ResponseEntity.ok(Response.successMessage("Review가 성공적으로 수정되었습니다."));
    }


    // 리뷰 삭제
    @DeleteMapping("/review/{reviewId}")
    public ResponseEntity<Response> deleteReview(@PathVariable Integer reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(Response.successMessage("Review가 삭제되었습니다."));
    }


}