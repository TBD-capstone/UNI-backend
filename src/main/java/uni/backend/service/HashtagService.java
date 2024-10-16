package uni.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uni.backend.domain.Hashtag;
import uni.backend.domain.MainCategory;
import uni.backend.domain.User;
import uni.backend.repository.HashtagRepository;
import uni.backend.repository.MainCategoryRepository;

import java.util.List;

@Service
public class HashtagService {

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private MainCategoryRepository mainCategoryRepository;



    public void addHashtagsToUser(User user, List<String> hashtags) {
        for (String hashtagName : hashtags) {
            Hashtag hashtag = hashtagRepository.findByHashtagName(hashtagName)
                    .orElseGet(() -> {
                        Hashtag newHashtag = new Hashtag();
                        newHashtag.setHashtagName(hashtagName);
                        return hashtagRepository.save(newHashtag);
                    });

            MainCategory mainCategory = new MainCategory();
            mainCategory.setHashtag(hashtag);

            mainCategoryRepository.save(mainCategory);
        }
    }

    public void editHashtagsToUser(User user, List<String> hashtags) {
        // To Do
    }

}
