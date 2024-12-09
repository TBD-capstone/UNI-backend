package uni.backend.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProfileTest {

    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = Profile.builder()
            .profileId(1)
            .user(new User()) // 필요에 따라 User 설정
            .imgProf("/profile-image.png")
            .imgBack("/basic_background.png")
            .region("Seoul")
            .description("Test description")
            .mainCategories(new ArrayList<>()) // 가변 리스트로 초기화
            .isVisible(true)
            .build();
    }

    @Test
    @DisplayName("Profile 생성 시 초기값 설정 확인")
    void givenNewProfile_whenCreated_thenDefaultValuesSet() {
        profile.prePersist();

        assertNotNull(profile.getCreatedAt());
        assertEquals(0, profile.getNumEmployment());
        assertEquals(0.0, profile.getStar());
        assertEquals("/profile-image.png", profile.getImgProf());
        assertEquals("/basic_background.png", profile.getImgBack());
    }

    @Test
    @DisplayName("MainCategory 추가 테스트")
    void givenMainCategory_whenAddMainCategory_thenCategoryAdded() {
        MainCategory mainCategory = new MainCategory();

        profile.addMainCategory(mainCategory);

        assertEquals(1, profile.getMainCategories().size());
        assertTrue(profile.getMainCategories().contains(mainCategory));
        assertEquals(profile, mainCategory.getProfile());
    }

    @Test
    @DisplayName("MainCategory 제거 테스트")
    void givenMainCategory_whenRemoveMainCategory_thenCategoryRemoved() {
        MainCategory mainCategory = new MainCategory();
        profile.addMainCategory(mainCategory);

        profile.removeMainCategory(mainCategory);

        assertEquals(0, profile.getMainCategories().size());
        assertFalse(profile.getMainCategories().contains(mainCategory));
        assertNull(mainCategory.getProfile());
    }

    @Test
    @DisplayName("Hashtag 리스트 가져오기 테스트")
    void givenMainCategories_whenGetHashtagStringList_thenReturnHashtags() {
        Hashtag hashtag1 = new Hashtag();
        hashtag1.setHashtagName("#Java");
        Hashtag hashtag2 = new Hashtag();
        hashtag2.setHashtagName("#Spring");

        MainCategory category1 = new MainCategory();
        category1.setHashtag(hashtag1);
        MainCategory category2 = new MainCategory();
        category2.setHashtag(hashtag2);

        profile.addMainCategory(category1);
        profile.addMainCategory(category2);

        List<String> hashtags = profile.getHashtagStringList();

        assertEquals(2, hashtags.size());
        assertTrue(hashtags.contains("#Java"));
        assertTrue(hashtags.contains("#Spring"));
    }

    @Test
    @DisplayName("Profile 숨기기/보이기 테스트")
    void whenHideOrShowProfile_thenVisibilityUpdated() {
        profile.hideProfile();
        assertFalse(profile.isVisible());

        profile.showProfile();
        assertTrue(profile.isVisible());
    }

    @Test
    @DisplayName("Profile 업데이트 시간 설정 테스트")
    void whenProfileUpdated_thenUpdatedAtSet() {
        profile.preUpdate();

        assertNotNull(profile.getUpdatedAt());
        assertTrue(profile.getUpdatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}
