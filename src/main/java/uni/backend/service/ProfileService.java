package uni.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Profile;
import uni.backend.repository.ProfileRepository;
import uni.backend.domain.User;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    @Autowired
    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }



    //    사용자 ID를 통해 Profile을 찾는 메서드
    public Optional<Profile> findProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId);
    }

    @Transactional
    public Profile updateProfile(Long userId, String content, String hashtag) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필이 존재하지 않습니다. : " + userId));

        profile.setContent(content);
        profile.setHashtag(hashtag);
        profile.setUpdatedAt(LocalDateTime.now());

        return profileRepository.save(profile);
    }
}
