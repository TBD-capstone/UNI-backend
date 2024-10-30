package uni.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Hashtag;
import uni.backend.domain.MainCategory;
import uni.backend.domain.Profile;
import uni.backend.domain.User;
import uni.backend.domain.dto.MainCategoryDTO;
import uni.backend.domain.dto.ProfileDTO;
import uni.backend.repository.HashtagRepository;
import uni.backend.repository.ProfileRepository;

import java.time.LocalDateTime;
import java.util.List;
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

    public ProfileDTO getProfileDTOByUserId(Integer userId) {
        Profile profile = profileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필이 존재하지 않습니다. : " + userId));

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setUserId(userId);
        profileDTO.setImgProf(profile.getImgProf());
        profileDTO.setImgBack(profile.getImgBack());
        profileDTO.setUniv(profile.getUser().getUniv());
        profileDTO.setRegion(profile.getRegion());
        profileDTO.setDescription(profile.getDescription());
        profileDTO.setNumEmployment(profile.getNumEmployment());
        profileDTO.setStar(profile.getStar());
        profileDTO.setTime(profile.getCreatedAt().toString());

        // 해시태그 리스트 설정
        List<String> hashtags = profile.getMainCategories().stream()
                .filter(mainCategory -> mainCategory.getHashtag() != null)
                .map(mainCategory -> mainCategory.getHashtag().getHashtagName())
                .collect(Collectors.toList());

        profileDTO.setHashtags(hashtags);

        return profileDTO;
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
    public Profile updateProfile(Integer userId, ProfileDTO profileDTO) {
        // 1. 사용자 ID로 프로필 조회
        Profile profile = profileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필이 존재하지 않습니다. : " + userId));

        // 2. ProfileDTO의 필드 값을 Profile에 설정
        profile.setImgProf(profileDTO.getImgProf()); // 프로필 이미지
        profile.setImgBack(profileDTO.getImgBack()); // 배경 이미지
        profile.setRegion(profileDTO.getRegion()); // 지역
        profile.setDescription(profileDTO.getDescription()); // 설명
        profile.setNumEmployment(profileDTO.getNumEmployment()); // 고용 횟수
        profile.setStar(profileDTO.getStar()); // 별점

        // 3. 해시태그 업데이트
        if (profileDTO.getHashtags() != null) {
            profile.getMainCategories().clear(); // 기존 카테고리 삭제
            for (String hashtagName : profileDTO.getHashtags()) {
                Hashtag hashtag = hashtagRepository.findByHashtagName(hashtagName)
                        .orElseGet(() -> {
                            // 해시태그가 없을 경우 기본 해시태그 추가
                            Hashtag newHashtag = new Hashtag();
                            newHashtag.setHashtagName(hashtagName);
                            return hashtagRepository.save(newHashtag); // 새로운 해시태그 저장
                        });

                MainCategory mainCategory = new MainCategory();
                mainCategory.setHashtag(hashtag);
                mainCategory.setProfile(profile);
                profile.addMainCategory(mainCategory); // 새로운 카테고리 추가
            }
        }

        // 4. 업데이트 시간 설정
        profile.setUpdatedAt(LocalDateTime.now());

        // 5. 프로필 저장
        return profileRepository.save(profile);
    }


}
