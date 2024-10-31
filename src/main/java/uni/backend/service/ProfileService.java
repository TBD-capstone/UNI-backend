package uni.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Hashtag;
import uni.backend.domain.MainCategory;
import uni.backend.domain.Profile;
import uni.backend.domain.dto.HashtagResponse;
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

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final HashtagRepository hashtagRepository;
    private final HashtagService hashtagService;

    @Autowired
    public ProfileService(ProfileRepository profileRepository,
                          HashtagRepository hashtagRepository,
                          HashtagService hashtagService) {
        this.profileRepository = profileRepository;
        this.hashtagRepository = hashtagRepository;
        this.hashtagService = hashtagService;  // HashtagService 초기화
    }

    // 사용자 ID를 통해 Profile을 찾는 메서드
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
        individualProfileResponse.setTime(profile.getCreatedAt().toString());

        // 해시태그 매핑을 List<String>으로 변경
        List<String> hashtags = getHashtagListFromProfile(profile);

        individualProfileResponse.setHashtags(hashtags); // DTO에 해시태그 세팅

        return individualProfileResponse;
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




//    "userId": Integer ,
//            "imgProf": String ,
//            "imgBack": String ,
//            "region": Map ,
//            "description": String ,
//            "time": String ,
//            "hashtags": List<String>


    @Transactional
    public Profile updateProfile(Integer userId, IndividualProfileResponse individualProfileResponse) {
        Profile profile = profileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필이 존재하지 않습니다. : " + userId));

        profile.setImgProf(individualProfileResponse.getImgProf());
        profile.setImgBack(individualProfileResponse.getImgBack());
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

    private List<Profile> getAllProfiles() {
        return profileRepository.findAll();
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
        List<Profile> list = getAllProfiles();

        homeDataResponse.setData(list.stream()
                .map(ProfileService::profileToHomeProfileResponse)
                .collect(Collectors.toList()));

        return homeDataResponse;
    }

}
