package uni.backend.service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Matching;
import uni.backend.domain.Review;
import uni.backend.domain.ReviewLikes;
import uni.backend.domain.User;
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

    public ReviewService(MatchingRepository matchingRepository, UserRepository userRepository,
        ReviewRepository reviewRepository,
        ReviewLikeRepository reviewLikeRepository) {
        this.matchingRepository = matchingRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.reviewLikeRepository = reviewLikeRepository;
    }

    @Transactional
    public Review createReview(Integer userId, Integer commenterId, String content, Integer star) {
        User profileOwner = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("프로필 주인을 찾을 수 없습니다. ID: " + userId));

        User commenter = userRepository.findById(commenterId)
            .orElseThrow(
                () -> new IllegalArgumentException("리뷰 작성자를 찾을 수 없습니다. ID: " + commenterId));

        Review review = Review.builder()
            .profileOwner(profileOwner)
            .commenter(commenter)
            .content(content)
            .star(star)
            .build();

        return reviewRepository.save(review);
    }


    public ReviewResponse convertToResponse(Review review) {
        boolean isDeleted = Boolean.TRUE.equals(review.getDeleted()); // 삭제 상태 확인

        return ReviewResponse.builder()
            .reviewId(review.getReviewId())
            .content(isDeleted ? null : review.getContent()) // 삭제된 경우 content는 null
            .star(isDeleted ? null : review.getStar()) // 삭제된 경우 star는 null
            .likes(review.getLikes())
            .profileOwnerId(review.getProfileOwner().getUserId())
            .profileOwnerName(review.getProfileOwner().getName())
            .commenterId(review.getCommenter().getUserId())
            .commenterName(review.getCommenter().getName())
            .deleted(review.getDeleted())
            .deletedTime(review.getDeletedTime())
            .updatedTime(review.getUpdatedTime())
            .deleteMessage(isDeleted ? "삭제된 리뷰입니다." : null) // 삭제 메시지 설정
            .build();
    }


    public List<Review> getReviewsByUserId(Integer userId) {
        return reviewRepository.findByProfileOwnerUserId(userId); // 모든 리뷰 조회
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
        return reviewRepository.save(review); // 변경된 리뷰 저장
    }

}
