package uni.backend.service;


import java.awt.print.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Hashtag;
import uni.backend.domain.MainCategory;
import uni.backend.domain.Profile;
import uni.backend.domain.Review;
import uni.backend.domain.Role;
import uni.backend.domain.User;
import uni.backend.domain.dto.HomeDataResponse;
import uni.backend.domain.dto.HomeProfileResponse;
import uni.backend.domain.dto.IndividualProfileResponse;
import uni.backend.repository.HashtagRepository;
import uni.backend.repository.ProfileRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import uni.backend.repository.ReviewRepository;
import uni.backend.repository.UserRepository;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final HashtagRepository hashtagRepository;
    private final AwsS3Service awsS3Service;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Autowired
    public ProfileService(ProfileRepository profileRepository,
        HashtagRepository hashtagRepository, AwsS3Service awsS3Service,
        ReviewRepository reviewRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.hashtagRepository = hashtagRepository;
        this.awsS3Service = awsS3Service;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    // 사용자 ID를 통해 Profile을 찾는 메서드
    public Optional<Profile> findProfileByUserId(Integer userId) {
        return profileRepository.findByUser_UserId(userId);
    }

    public IndividualProfileResponse getProfileDTOByUserId(Integer userId) {
        Profile profile = profileRepository.findByUser_UserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("프로필이 존재하지 않습니다. : " + userId));

        IndividualProfileResponse individualProfileResponse = new IndividualProfileResponse();
        individualProfileResponse.setUserId(userId);
        individualProfileResponse.setUserName(profile.getUser().getName()); // userName 추가
        individualProfileResponse.setImgProf(profile.getImgProf());
        individualProfileResponse.setImgBack(profile.getImgBack());
        individualProfileResponse.setUniv(profile.getUser().getUnivName());
        individualProfileResponse.setRegion(profile.getRegion());
        individualProfileResponse.setDescription(profile.getDescription());
        individualProfileResponse.setNumEmployment(profile.getNumEmployment());
        individualProfileResponse.setStar(profile.getStar());
//        individualProfileResponse.setTime(profile.getCreatedAt().toString());
        individualProfileResponse.setTime(profile.getTime());

        individualProfileResponse.setImgProf(
            awsS3Service.getImageUrl(profile.getImgProf()));  // 프로필 이미지 URL 가져오기
        individualProfileResponse.setImgBack(
            awsS3Service.getImageUrl(profile.getImgBack()));  // 배경 이미지 URL 가져오기
        individualProfileResponse.setHashtags(profile.getHashtagStringList());

        return individualProfileResponse;
    }

    // 사용자 저장
    @Transactional
    public void save(Profile profile) {
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        profileRepository.save(profile);
    }


    @Transactional
    public Profile updateProfile(Integer userId,
        IndividualProfileResponse individualProfileResponse) {
        Profile profile = profileRepository.findByUser_UserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("프로필이 존재하지 않습니다. : " + userId));
        // 이미지 URL을 프로필에 반영
        profile.setImgProf(individualProfileResponse.getImgProf());  // 프로필 이미지 URL 설정
        profile.setImgBack(individualProfileResponse.getImgBack());  // 배경 이미지 URL 설정

        profile.setRegion(individualProfileResponse.getRegion());
        profile.setTime(individualProfileResponse.getTime());
        profile.setDescription(individualProfileResponse.getDescription());

        // 해시태그 업데이트
        if (individualProfileResponse.getHashtags() != null) {
            profile.getMainCategories().clear(); // 기존 카테고리 삭제
            for (String hashtagName : individualProfileResponse.getHashtags()) {
                Hashtag hashtag = hashtagRepository.findByHashtagName(hashtagName)
                    .orElseGet(() -> {
                        // 해시태그가 없을 경우 기본 해시태그 추가
                        Hashtag newHashtag = new Hashtag();
                        newHashtag.setHashtagName(hashtagName);
                        return hashtagRepository.save(newHashtag);
                    });

                MainCategory mainCategory = new MainCategory();
                mainCategory.setHashtag(hashtag);
                mainCategory.setProfile(profile);
                profile.addMainCategory(mainCategory); // 새로운 카테고리 추가
            }
        }

        profile.setUpdatedAt(LocalDateTime.now());
        return profileRepository.save(profile);
    }


    @Transactional
    public void updateProfileStar(Integer profileOwnerId) {
        List<Review> reviews = reviewRepository.findByProfileOwnerUserId(profileOwnerId);

        if (reviews.isEmpty()) {
            throw new IllegalArgumentException("해당 유저에 대한 리뷰가 없습니다.");
        }

        double averageStar = reviews.stream()
            .filter(review -> !Boolean.TRUE.equals(review.getDeleted())) // 삭제되지 않은 리뷰만 포함
            .mapToInt(Review::getStar)
            .average()
            .orElse(0.0);

        // 소수점 둘째 자리로 반올림
        double roundedAverageStar = Math.round(averageStar * 100.0) / 100.0;

        User profileOwner = userRepository.findById(profileOwnerId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
        profileOwner.getProfile().setStar(roundedAverageStar); // 반올림된 값 저장
    }
}
