package uni.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Hashtag;
import uni.backend.domain.MainCategory;
import uni.backend.domain.Profile;
import uni.backend.domain.User;
import uni.backend.repository.HashtagRepository;
import uni.backend.repository.MainCategoryRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HashtagService {

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private MainCategoryRepository mainCategoryRepository;

    @Transactional
    public void addHashtagsToProfile(Profile profile, List<String> hashtags) {
        // 기존 mainCategories를 clear() 호출로 초기화
        profile.getMainCategories().clear();  // 수정된 부분

        for (String hashtagName : hashtags) {
            Hashtag hashtag = hashtagRepository.findByHashtagName(hashtagName)
                .orElseGet(() -> {
                    Hashtag newHashtag = new Hashtag();
                    newHashtag.setHashtagName(hashtagName);
                    return hashtagRepository.save(newHashtag);
                });

            MainCategory mainCategory = new MainCategory();
            mainCategory.setHashtag(hashtag);
            mainCategory.setProfile(profile);  // Profile과 연관 설정 (수정된 부분)
            profile.addMainCategory(mainCategory);  // Profile에 MainCategory 추가

            mainCategoryRepository.save(mainCategory);
        }
    }

    public void editHashtagsToUser(User user, List<String> hashtags) {
        // TODO: 사용자 해시태그 수정 메서드 구현
    }

    @Transactional
    public List<String> findUsersByHashtags(List<String> hashtags) {
        List<Hashtag> hashtagList = new ArrayList<>();

        for (String hashtagName : hashtags) {
            Optional<Hashtag> optionalHashtag = hashtagRepository.findByHashtagName(hashtagName);
            if (optionalHashtag.isEmpty()) {
                return List.of();
            }
            hashtagList.add(optionalHashtag.get());
        }

        if (hashtagList.isEmpty()) {
            return List.of();
        }

        List<Profile> initialProfiles = new ArrayList<>(
            hashtagList.get(0).getMainCategories().stream()
                .map(MainCategory::getProfile)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));  // 수정된 부분

        for (Hashtag hashtag : hashtagList.subList(1, hashtagList.size())) {
            List<Profile> profilesWithHashtag = hashtag.getMainCategories().stream()
                .map(MainCategory::getProfile)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            initialProfiles.retainAll(profilesWithHashtag);

            if (initialProfiles.isEmpty()) {
                return List.of();
            }
        }

        return initialProfiles.stream()
            .map(profile -> profile.getUser().getName())
            .distinct()
            .collect(Collectors.toList());
    }
}
