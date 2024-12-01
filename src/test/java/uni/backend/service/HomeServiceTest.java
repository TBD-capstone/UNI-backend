package uni.backend.service;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import uni.backend.domain.Hashtag;
import uni.backend.domain.MainCategory;
import uni.backend.domain.Profile;
import uni.backend.domain.User;
import uni.backend.domain.dto.HomeProfileResponse;
import uni.backend.repository.ProfileRepository;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class HomeServiceTest {

    @InjectMocks
    private HomeService homeService;

    @Mock
    private ProfileRepository profileRepository;

    private Profile profile1;
    private Profile profile2;
    private Profile profile3;
    private User user1;
    private User user2;
    private User user3;
    private List<String> hashtags;

    private MainCategory createMainCategoryWithHashtag(String hashtagName) {
        Hashtag hashtag = new Hashtag();
        hashtag.setHashtagName(hashtagName);

        MainCategory mainCategory = new MainCategory();
        mainCategory.setHashtag(hashtag);

        return mainCategory;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 사용자와 프로필 생성
        user1 = User.builder().userId(1).name("User1").univName("University A").build();
        user2 = User.builder().userId(2).name("User2").univName("University A").build();
        user3 = User.builder().userId(3).name("User3").univName("University B").build();

        profile1 = new Profile();
        profile1.setUser(user1);
        profile1.setImgProf("/img1.png");
        profile1.setStar(4.5);

        profile2 = new Profile();
        profile2.setUser(user2);
        profile2.setImgProf("/img2.png");
        profile2.setStar(3.0);

        profile3 = new Profile();
        profile3.setUser(user3);
        profile3.setImgProf("/img3.png");
        profile3.setStar(5.0);

        // 해시태그 추가
        hashtags = List.of("Hashtag1", "Hashtag2");
        hashtags.forEach(
            hashtagName -> {
                profile1.addMainCategory(createMainCategoryWithHashtag(hashtagName));
                profile2.addMainCategory(createMainCategoryWithHashtag(hashtagName));
            });
    }

    @Test
    @DisplayName("Profile -> HomeProfileResponse 변환 성공 테스트")
    void profileToHomeProfileResponse_성공() {
        // when
        HomeProfileResponse homeProfileResponse = homeService.profileToHomeProfileResponse(
            profile1);

        // then
        assertEquals("User1", homeProfileResponse.getUsername());
        assertEquals("/img1.png", homeProfileResponse.getImgProf());
        assertEquals(4.5, homeProfileResponse.getStar());
        assertEquals("University A", homeProfileResponse.getUnivName());
        assertTrue(homeProfileResponse.getHashtags().containsAll(hashtags));
    }

    @Test
    @DisplayName("대학교 이름과 해시태그로 검색 성공 테스트 - 정렬 기준: 최신순")
    void searchByUnivNameAndHashtags_최신순_성공() {
        // Given
        List<Profile> profiles = List.of(profile1, profile2);
        Page<Profile> page = new PageImpl<>(profiles, PageRequest.of(0, 10), profiles.size());

        when(profileRepository.findByUnivNameAndHashtags(anyString(), any(), anyInt(), any()))
            .thenReturn(page);

        // When
        Page<HomeProfileResponse> result = homeService.searchByUnivNameAndHashtags(
            "University A", hashtags, 1, "newest");

        // Then
        assertEquals(2, result.getTotalElements());
        verify(profileRepository).findByUnivNameAndHashtags(anyString(), any(), anyInt(), any());
    }

    @Test
    @DisplayName("대학교 이름과 해시태그로 검색 성공 테스트 - 정렬 기준: 높은 평점순")
    void searchByUnivNameAndHashtags_높은평점순_성공() {
        // Given
        List<Profile> profiles = List.of(profile3, profile1);
        Page<Profile> page = new PageImpl<>(profiles, PageRequest.of(0, 10), profiles.size());

        when(profileRepository.findByUnivNameAndHashtags(anyString(), any(), anyInt(), any()))
            .thenReturn(page);

        // When
        Page<HomeProfileResponse> result = homeService.searchByUnivNameAndHashtags(
            "University A", hashtags, 1, "highest_rating");

        // Then
        assertEquals(2, result.getTotalElements());
        assertEquals("User3", result.getContent().get(0).getUsername());
        verify(profileRepository).findByUnivNameAndHashtags(anyString(), any(), anyInt(), any());
    }

    @Test
    @DisplayName("대학교 이름과 해시태그로 검색 성공 테스트 - 정렬 기준: 낮은 평점순")
    void searchByUnivNameAndHashtags_낮은평점순_성공() {
        // Given
        List<Profile> profiles = List.of(profile2, profile1);
        Page<Profile> page = new PageImpl<>(profiles, PageRequest.of(0, 10), profiles.size());

        when(profileRepository.findByUnivNameAndHashtags(anyString(), any(), anyInt(), any()))
            .thenReturn(page);

        // When
        Page<HomeProfileResponse> result = homeService.searchByUnivNameAndHashtags(
            "University A", hashtags, 1, "lowest_rating");

        // Then
        assertEquals(2, result.getTotalElements());
        assertEquals("User2", result.getContent().get(0).getUsername());
        verify(profileRepository).findByUnivNameAndHashtags(anyString(), any(), anyInt(), any());
    }
}
