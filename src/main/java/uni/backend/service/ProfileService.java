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
    private final HashtagService hashtagService;
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

    /**
     * 사용자 ID를 통해 프로필 조회
     *
     * @param userId 사용자 ID
     * @return 해당 유저의 프로필
     */
    @Transactional(readOnly = true)
    public Optional<Profile> findProfileByUserId(Integer userId) {
        return profileRepository.findByUser_UserId(userId);
    }

    private static List<String> getHashtagListFromProfile(Profile profile) {
        List<String> hashtags;
        hashtags = profile.getMainCategories().stream()
            .map(mainCategory -> {
                Hashtag hashtag = mainCategory.getHashtag();
                return hashtag != null ? hashtag.getHashtagName() : null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return hashtags;
    }

    /**
     * 사용자 ID를 통해 프로필 DTO 조회
     *
     * @param userId 사용자 ID
     * @return 프로필 DTO
     */
    @Transactional(readOnly = true)
    public IndividualProfileResponse getProfileDTOByUserId(Integer userId) {
        Profile profile = profileRepository.findByUser_UserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("프로필이 존재하지 않습니다. : " + userId));

        if (!profile.isVisible()) {
            throw new IllegalStateException("해당 프로필은 비공개 상태입니다.");
        }

        return IndividualProfileResponse.builder()
            .userId(userId)
            .userName(profile.getUser().getName())
            .imgProf(profile.getImgProf())
            .imgBack(profile.getImgBack())
            .univ(profile.getUser().getUnivName())
            .region(profile.getRegion())
            .description(profile.getDescription())
            .numEmployment(profile.getNumEmployment())
            .star(profile.getStar())
            .time(profile.getTime())
            .hashtags(getHashtagListFromProfile(profile))
            .isVisible(profile.isVisible())
            .build();
    }


    public void addHashtagsToProfile(Profile profile, List<String> hashtags) {
        // 해시태그를 프로필에 추가
        hashtagService.addHashtagsToProfile(profile, hashtags);
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

    private static HomeProfileResponse profileToHomeProfileResponse(Profile profile) {
        HomeProfileResponse homeProfileResponse = new HomeProfileResponse();
        List<String> hashtags = getHashtagListFromProfile(profile);
        homeProfileResponse.setUsername(profile.getUser().getName());
        homeProfileResponse.setImgProf(profile.getImgProf());
        homeProfileResponse.setStar(profile.getStar());
        homeProfileResponse.setUnivName(profile.getUser().getUnivName());
        homeProfileResponse.setHashtags(hashtags);
        homeProfileResponse.setUserId(profile.getUser().getUserId());

        return homeProfileResponse;
    }

    public HomeDataResponse getHomeDataProfiles() {
        HomeDataResponse homeDataResponse = new HomeDataResponse();
        List<Profile> list = profileRepository.findByUser_Role(Role.KOREAN);

        homeDataResponse.setData(list.stream()
            .map(ProfileService::profileToHomeProfileResponse)
            .collect(Collectors.toList()));

        return homeDataResponse;
    }

}
