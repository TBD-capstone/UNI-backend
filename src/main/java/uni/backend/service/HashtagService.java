package uni.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Hashtag;
import uni.backend.domain.MainCategory;
import uni.backend.domain.Profile;
import uni.backend.repository.HashtagRepository;
import uni.backend.repository.MainCategoryRepository;

import java.util.*;

@Service
public class HashtagService {

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private MainCategoryRepository mainCategoryRepository;

    @Transactional
    public void addHashtagsToProfile(Profile profile, List<String> hashtags) {
        profile.getMainCategories().clear();

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
            profile.addMainCategory(mainCategory);

            mainCategoryRepository.save(mainCategory);
        }
    }
}
