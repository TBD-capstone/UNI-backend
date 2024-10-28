package uni.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.MainCategory;
import uni.backend.domain.Profile;
import uni.backend.repository.ProfileRepository;
import uni.backend.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    @Autowired
    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }



    //    사용자 ID를 통해 Profile을 찾는 메서드
    public Optional<Profile> findProfileByUserId(Integer userId) {
        return profileRepository.findByUser_UserId(userId);
    }



    // 프로필 업데이트
    @Transactional
    public Profile updateProfile(Integer userId, String content, List<MainCategory> mainCategories) {
        Profile profile = profileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필이 존재하지 않습니다. : " + userId));

        profile.setContent(content);
        profile.setUpdatedAt(LocalDateTime.now());
        profile.setMainCategories(mainCategories);

        return profileRepository.save(profile);
    }
}
