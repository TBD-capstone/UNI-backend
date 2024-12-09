package uni.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Matching;
import uni.backend.domain.Review;
import uni.backend.domain.ReviewLikes;
import uni.backend.domain.ReviewReply;
import uni.backend.domain.User;
import uni.backend.domain.dto.ReviewReplyResponse;
import uni.backend.domain.dto.ReviewResponse;
import uni.backend.repository.MatchingRepository;
import uni.backend.repository.ReviewLikeRepository;
import uni.backend.repository.ReviewRepository;
import uni.backend.repository.UserRepository;

@Service
public class ReviewService {

    private final MatchingRepository matchingRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ProfileService profileService;

    public ReviewService(MatchingRepository matchingRepository, UserRepository userRepository,
        ReviewRepository reviewRepository, ReviewLikeRepository reviewLikeRepository,
        ProfileService profileService) {
        this.matchingRepository = matchingRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.profileService = profileService;
    }

    @Transactional
    public ReviewResponse createReview(Integer matchingId, Integer profileOwnerId,
        Integer commenterId, String content, Integer star) {

        // 리뷰 생성
        Matching matching = matchingRepository.findById(matchingId)
            .orElseThrow(() -> new IllegalArgumentException("해당 매칭을 찾을 수 없습니다."));

        if (matching.getReview() != null) {
            throw new IllegalArgumentException("이미 리뷰가 존재합니다.");
        }

        User profileOwner = userRepository.findById(profileOwnerId)
            .orElseThrow(() -> new IllegalArgumentException("프로필 주인을 찾을 수 없습니다."));

        User commenter = userRepository.findById(commenterId)
            .orElseThrow(() -> new IllegalArgumentException("리뷰 작성자를 찾을 수 없습니다."));

        Review review = Review.builder()
            .matching(matching)
            .profileOwner(profileOwner)
            .commenter(commenter)
            .content(content)
            .star(star)
            .build();

        matching.setReview(review);
        matching.setStatus(Matching.Status.ENDED);
        Review savedReview = reviewRepository.save(review);

        // 프로필 별점 업데이트
        profileService.updateProfileStar(profileOwnerId);

        // 생성된 리뷰를 응답 객체로 변환
        return convertToResponse(savedReview);
    }

    public ReviewResponse convertToResponse(Review review) {
        boolean isDeleted = Boolean.TRUE.equals(review.getDeleted());
        boolean isBlind = Boolean.TRUE.equals(review.getIsBlind());

        String content = null;
        String deleteMessage = null;
        Integer star = null;

        if (isDeleted) {
            deleteMessage = "삭제된 리뷰입니다.";
        } else if (isBlind) {
            content = "블라인드 처리된 리뷰입니다.";
        } else {
            content = review.getContent();
            star = review.getStar();
        }

        return ReviewResponse.builder()
            .reviewId(review.getReviewId())
            .content(content)
            .star(star)
            .likes(review.getLikes())
            .profileOwnerId(review.getProfileOwner().getUserId())
            .profileOwnerName(review.getProfileOwner().getName())
            .commenterId(review.getCommenter().getUserId())
            .commenterName(review.getCommenter().getName())
            .commenterImgProf(review.getCommenter().getProfile().getImgProf())
            .deleted(review.getDeleted())
            .deletedTime(review.getDeletedTime())
            .updatedTime(review.getUpdatedTime())
            .deleteMessage(deleteMessage)
            .replies(getReplyResponses(review)) // 대댓글 리스트 변환 추가
            .build();
    }

    // 대댓글 변환 로직
    protected List<ReviewReplyResponse> getReplyResponses(Review review) {
        List<ReviewReply> replies = review.getReplies(); // ReviewReply 사용
        if (replies == null) {
            return new ArrayList<>(); // null이면 빈 리스트 반환
        }

        return replies.stream()
            .map(reply -> {
                boolean isReplyDeleted = Boolean.TRUE.equals(reply.getDeleted());
                boolean isReplyBlind = Boolean.TRUE.equals(reply.getIsBlind());

                String content = null;
                String deleteMessage = null;

                if (isReplyDeleted) {
                    deleteMessage = "삭제된 대댓글입니다.";
                } else if (isReplyBlind) {
                    deleteMessage = "블라인드 처리된 대댓글입니다.";
                } else {
                    content = reply.getContent();
                }

                return ReviewReplyResponse.builder()
                    .replyId(reply.getReplyId())
                    .reviewId(reply.getReview().getReviewId()) // ReviewReply와 Review 간 관계
                    .commenterId(reply.getCommenter().getUserId())
                    .commenterName(reply.getCommenter().getName())
                    .commenterImgProf(reply.getCommenter().getProfile().getImgProf())
                    .content(content)
                    .deleted(reply.getDeleted())
                    .deletedTime(reply.getDeletedTime())
                    .updatedTime(reply.getUpdatedTime())
                    .deleteMessage(deleteMessage)
                    .likes(reply.getLikes())
                    .build();
            })
            .collect(Collectors.toList());
    }


    //List<Review>를 반환
    @Transactional(readOnly = true)
    public List<Review> getReviewsByUserId(Integer userId) {
        return reviewRepository.findByProfileOwnerUserId(userId);
    }

    public List<ReviewResponse> getReviewResponsesByUserId(Integer userId) {
        List<Review> reviews = getReviewsByUserId(userId); // 특정 유저의 리뷰 조회
        return reviews.stream()
            .map(this::convertToResponse) // 리뷰 -> ReviewResponse 변환
            .collect(Collectors.toList());
    }

    @Transactional
    public Review toggleLike(Integer reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. ID: " + reviewId));

        if (review.getLikes() == null) {
            review.setLikes(0L);
        }

        Optional<ReviewLikes> existingLike = reviewLikeRepository.findByUserAndReview(user, review);
        if (existingLike.isPresent()) {
            // 좋아요 취소
            reviewLikeRepository.delete(existingLike.get());
            review.decreaseLikes(); // 좋아요 수 감소
        } else {
            // 좋아요 추가
            ReviewLikes like = new ReviewLikes();
            like.setUser(user);
            like.setReview(review);
            reviewLikeRepository.save(like);
            review.increaseLikes(); // 좋아요 수 증가
        }

        return reviewRepository.save(review); // 업데이트된 리뷰 반환
    }

    @Transactional
    public void deleteReview(Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("해당 Review가 존재하지 않습니다."));

        review.setDeleted(true); // 삭제 플래그 설정
        review.setDeletedTime(LocalDateTime.now()); // 삭제 시간 설정

        // 프로필 별점 업데이트
        profileService.updateProfileStar(review.getProfileOwner().getUserId());
    }

    @Transactional
    public Review updateReviewContent(Integer reviewId, String newContent, Integer star) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. ID: " + reviewId));

        if (star < 1 || star > 5) {
            throw new IllegalArgumentException("별점은 1~5 사이여야 합니다.");
        }

        review.updateContent(newContent); // 새로운 내용으로 업데이트
        review.setStar(star);             // 별점 업데이트
        Review updatedReview = reviewRepository.save(review);

        // 프로필 별점 업데이트
        profileService.updateProfileStar(review.getProfileOwner().getUserId());

        return updatedReview;
    }
}
