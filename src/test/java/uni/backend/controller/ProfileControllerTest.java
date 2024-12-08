package uni.backend.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uni.backend.domain.Profile;
import uni.backend.domain.dto.IndividualProfileResponse;
import uni.backend.domain.dto.MeResponse;
import uni.backend.service.*;

import java.util.Optional;

class ProfileControllerTest {

    @InjectMocks
    private ProfileController profileController;

    @Mock
    private ProfileService profileService;
    @Mock
    private AwsS3Service awsS3Service;
    @Mock
    private PageTranslationService pageTranslationService;
    @Mock
    private TranslationService translationService;
    @Mock
    private UserServiceImpl userService;

    @Mock
    private MultipartFile profileImage;
    @Mock
    private MultipartFile backgroundImage;

    private Profile profile;
    private MeResponse meResponse;
    private IndividualProfileResponse individualProfileResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        profile = new Profile();
        profile.setProfileId(1);
        profile.setVisible(true);

        meResponse = new MeResponse();
        meResponse.setUserId(1);
        meResponse.setName("Test User");

        individualProfileResponse = new IndividualProfileResponse();
        individualProfileResponse.setUserId(1);
        individualProfileResponse.setUserName("Test User");
    }

    @Test
    @DisplayName("사용자 정보 조회")
    void testGetCurrentUser() {
        // given
        when(userService.getCurrentUserProfile()).thenReturn(meResponse);

        // when
        ResponseEntity<MeResponse> response = profileController.getCurrentUser();

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(meResponse, response.getBody());
    }

    @Test
    @DisplayName("사용자 프로필 업데이트")
    void testUpdateProfile() {
        // given
        when(profileService.updateProfileImage(1, profileImage, backgroundImage)).thenReturn(
            profile);
        when(profileService.getProfileDTOByUserId(1)).thenReturn(individualProfileResponse);

        // when
        ResponseEntity<IndividualProfileResponse> response = profileController.updateProfile(1,
            profileImage, backgroundImage);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(individualProfileResponse, response.getBody());
    }

    @Test
    @DisplayName("특정 사용자 프로필 조회")
    void testGetUserProfile() {
        // given
        when(profileService.findProfileByUserId(1)).thenReturn(Optional.of(profile));
        when(translationService.determineTargetLanguage("en")).thenReturn("en");
        when(profileService.getProfileDTOByUserId(1)).thenReturn(individualProfileResponse);

        // when
        ResponseEntity<IndividualProfileResponse> response = profileController.getUserProfile(1,
            "en");

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(individualProfileResponse, response.getBody());
    }

    @Test
    @DisplayName("비공개 프로필 조회 시 예외 발생")
    void testGetUserProfilePrivate() {
        // given
        profile.setVisible(false);
        when(profileService.findProfileByUserId(1)).thenReturn(Optional.of(profile));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            profileController.getUserProfile(1, null);
        });

        assertEquals("해당 프로필은 비공개 상태입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("사용자 프로필 정보 수정")
    void testUpdateUserProfile() {
        // given
        IndividualProfileResponse updatedProfileDto = new IndividualProfileResponse();
        updatedProfileDto.setUserName("Updated User");
        when(profileService.updateProfile(1, updatedProfileDto)).thenReturn(profile);
        when(profileService.getProfileDTOByUserId(1)).thenReturn(individualProfileResponse);

        // when
        ResponseEntity<IndividualProfileResponse> response = profileController.updateUserProfile(
            "1", updatedProfileDto);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(individualProfileResponse, response.getBody());
    }
}