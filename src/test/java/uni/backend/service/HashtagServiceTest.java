package uni.backend.service;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import uni.backend.domain.Hashtag;
import uni.backend.domain.MainCategory;
import uni.backend.domain.Profile;
import uni.backend.repository.HashtagRepository;
import uni.backend.repository.MainCategoryRepository;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class HashtagServiceTest {

    @Mock
    private HashtagRepository hashtagRepository;

    @Mock
    private MainCategoryRepository mainCategoryRepository;

    @InjectMocks
    private HashtagService hashtagService;

    private Profile profile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        profile = new Profile();
    }

    @DisplayName("새로운 해시태그 추가 시 새로운 MainCategory 생성")
    @Test
    void givenNewHashtags_whenAddHashtagsToProfile_thenMainCategoryIsCreated() {
        // given
        String hashtagName = "testHashtag";
        List<String> hashtags = Arrays.asList(hashtagName);

        Hashtag hashtag = new Hashtag();
        hashtag.setHashtagName(hashtagName);
        when(hashtagRepository.findByHashtagName(hashtagName)).thenReturn(Optional.empty());
        when(hashtagRepository.save(any(Hashtag.class))).thenReturn(hashtag);

        // when
        hashtagService.addHashtagsToProfile(profile, hashtags);

        // then
        assertEquals(1, profile.getMainCategories().size());
        MainCategory mainCategory = profile.getMainCategories().iterator().next();
        assertNotNull(mainCategory.getHashtag());
        assertEquals(hashtagName, mainCategory.getHashtag().getHashtagName());

        verify(hashtagRepository).findByHashtagName(hashtagName);
        verify(hashtagRepository).save(any(Hashtag.class));
        verify(mainCategoryRepository).save(any(MainCategory.class));
    }

    @DisplayName("이미 존재하는 해시태그일 경우 새로운 해시태그 객체 생성 없이 MainCategory 추가")
    @Test
    void givenExistingHashtags_whenAddHashtagsToProfile_thenMainCategoryIsAdded() {
        // given
        String hashtagName = "existingHashtag";
        List<String> hashtags = Arrays.asList(hashtagName);

        Hashtag hashtag = new Hashtag();
        hashtag.setHashtagName(hashtagName);
        when(hashtagRepository.findByHashtagName(hashtagName)).thenReturn(Optional.of(hashtag));

        // when
        hashtagService.addHashtagsToProfile(profile, hashtags);

        // then
        assertEquals(1, profile.getMainCategories().size());
        MainCategory mainCategory = profile.getMainCategories().iterator().next();
        assertNotNull(mainCategory.getHashtag());
        assertEquals(hashtagName, mainCategory.getHashtag().getHashtagName());

        verify(hashtagRepository).findByHashtagName(hashtagName);
        verify(mainCategoryRepository).save(any(MainCategory.class));
    }

    @DisplayName("해시태그 목록이 비어있을 경우 MainCategory가 추가되지 않음")
    @Test
    void givenEmptyHashtags_whenAddHashtagsToProfile_thenNoMainCategoryIsAdded() {
        // given
        List<String> hashtags = Arrays.asList();

        // when
        hashtagService.addHashtagsToProfile(profile, hashtags);

        // then
        assertEquals(0, profile.getMainCategories().size());
        verify(hashtagRepository, never()).findByHashtagName(anyString());
        verify(mainCategoryRepository, never()).save(any(MainCategory.class));
    }
}