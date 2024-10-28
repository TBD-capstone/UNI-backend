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
        for (String hashtagName : hashtags) {
            Hashtag hashtag = hashtagRepository.findByHashtagName(hashtagName)
                    .orElseGet(() -> {
                        Hashtag newHashtag = new Hashtag();
                        newHashtag.setHashtagName(hashtagName);
                        return hashtagRepository.save(newHashtag);
                    });

            MainCategory mainCategory = new MainCategory();
            mainCategory.setHashtag(hashtag);
            mainCategory.setProfile(profile);

            mainCategoryRepository.save(mainCategory);
        }
    }

    public void editHashtagsToUser(User user, List<String> hashtags) {
        // To Do
    }

    @Transactional
    public List<String> findUsersByHashtags(List<String> hashtags) {
        List<Hashtag> hashtagList = new ArrayList<>();

        // 입력된 해시태그 각각을 확인하면서 유효한 해시태그인지 검사
        for (String hashtagName : hashtags) {
            Optional<Hashtag> optionalHashtag = hashtagRepository.findByHashtagName(hashtagName);

            if (optionalHashtag.isEmpty())
                return List.of();
            hashtagList.add(optionalHashtag.get());
        }

        // 첫 번째 해시태그의 MainCategory에서 연결된 Profile을 추출
        List<Profile> initialProfiles = new java.util.ArrayList<>(hashtagList.getFirst().getMainCategories().stream()
                .map(MainCategory::getProfile)
                .filter(Objects::nonNull)
                .toList());

        System.out.println("0: " + initialProfiles.getFirst().getUser().getName());

        // 나머지 해시태그들에 대해 공통으로 연결된 Profile만 남김
        for (Hashtag hashtag : hashtagList.subList(1, hashtagList.size())) {
            System.out.println("Hello..?");
            List<Profile> profilesWithHashtag = hashtag.getMainCategories().stream()
                    .map(MainCategory::getProfile)
                    .filter(Objects::nonNull)
                    .toList();

            // 현재 검색 중인 해시태그의 Profile 목록과 교집합만 남김
            System.out.println(profilesWithHashtag);
            initialProfiles.retainAll(profilesWithHashtag);
            System.out.println(initialProfiles);

            // 교집합이 비어있으면 더 이상 검색할 필요 없으므로 바로 빈 리스트 반환
            if (initialProfiles.isEmpty()) {
                return List.of();
            }
        }

        // 공통으로 모든 해시태그를 가진 Profile에서 사용자 이름 추출
        return initialProfiles.stream()
                .map(profile -> profile.getUser().getName())
                .distinct()  // 중복 제거
                .toList();
    }

}
