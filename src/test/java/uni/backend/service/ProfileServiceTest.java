package uni.backend.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import uni.backend.domain.Hashtag;
import uni.backend.domain.MainCategory;
import uni.backend.domain.Profile;
import uni.backend.domain.Review;
import uni.backend.domain.Role;
import uni.backend.domain.User;
import uni.backend.domain.dto.HomeDataResponse;
import uni.backend.domain.dto.HomeProfileResponse;
import uni.backend.domain.dto.IndividualProfileResponse;
import uni.backend.repository.HashtagRepository;
import uni.backend.repository.ProfileRepository;
import uni.backend.repository.ReviewRepository;
import uni.backend.repository.UserRepository;

class ProfileServiceTest {

    @InjectMocks
    private ProfileService profileService;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private HashtagRepository hashtagRepository;

    @Mock
    private AwsS3Service awsS3Service;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    private Profile profile;
    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setUserId(1);
        user.setName("Test User");
        user.setUnivName("Test University");

        profile = new Profile();
        profile.setUser(user);
        profile.setImgProf("profileImg.png");
        profile.setImgBack("backgroundImg.png");
        profile.setRegion("Seoul");
        profile.setDescription("Test Description");
        profile.setNumEmployment(3);
        profile.setStar(4.5);
        profile.setVisible(true);
    }

    @Test
    void 프로필_DTO_조회_성공() {
        // Given
        Integer userId = user.getUserId();
        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profile));

        // When
        IndividualProfileResponse response = profileService.getProfileDTOByUserId(userId);

        // Then
        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals("Test User", response.getUserName());
        assertEquals("Test University", response.getUniv());
        assertEquals("profileImg.png", response.getImgProf());
        assertEquals("backgroundImg.png", response.getImgBack());
        assertEquals("Seoul", response.getRegion());
        assertEquals(3, response.getNumEmployment());
        assertEquals(4.5, response.getStar());
    }

    @Test
    void 프로필_DTO_조회_예외_프로필_없음() {
        // Given
        Integer userId = user.getUserId();
        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            profileService.getProfileDTOByUserId(userId);
        });

        assertEquals("프로필이 존재하지 않습니다. : " + userId, exception.getMessage());
    }

    @Test
    void 프로필_이미지_업데이트_성공() {
        // Given
        Integer userId = 1;

        Profile profile = new Profile();
        profile.setUser(new User());

        MultipartFile profileImageMock = mock(MultipartFile.class);
        MultipartFile backgroundImageMock = mock(MultipartFile.class);

        when(profileImageMock.isEmpty()).thenReturn(false); // 파일 비어있지 않음
        when(profileImageMock.getOriginalFilename()).thenReturn("profile-image.jpg"); // 파일명 설정

        when(backgroundImageMock.isEmpty()).thenReturn(false);
        when(backgroundImageMock.getOriginalFilename()).thenReturn("background-image.jpg");

        // Mock S3 업로드 설정
        when(awsS3Service.upload(profileImageMock, "profile", userId)).thenReturn(
            "profile-image-url");
        when(awsS3Service.upload(backgroundImageMock, "background", userId)).thenReturn(
            "background-image-url");

        // ProfileRepository Mock 설정
        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(
            invocation -> invocation.getArgument(0));

        // When
        Profile updatedProfile = profileService.updateProfileImage(userId, profileImageMock,
            backgroundImageMock);

        assertNotNull(updatedProfile); // Profile 객체가 null이 아님을 검증
        assertEquals("profile-image-url", updatedProfile.getImgProf()); // 프로필 이미지 URL 검증
        assertEquals("background-image-url", updatedProfile.getImgBack()); // 배경 이미지 URL 검증
    }

    @Test
    void 프로필_정보_업데이트_성공() {
        // Given
        Integer userId = 1;
        Profile profile = new Profile();
        profile.setUser(new User()); // User와 연결
        IndividualProfileResponse profileDto = IndividualProfileResponse.builder()
            .region("Seoul")
            .time("Morning")
            .description("Updated description")
            .hashtags(List.of("hashtag1", "hashtag2"))
            .build();

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(
            invocation -> invocation.getArgument(0));

        // When
        Profile updatedProfile = profileService.updateProfile(userId, profileDto);

        // Then
        assertNotNull(updatedProfile);
        assertEquals("Seoul", updatedProfile.getRegion());
        assertEquals("Morning", updatedProfile.getTime());
        assertEquals("Updated description", updatedProfile.getDescription());
    }

    @Test
    void 프로필_별점_업데이트_성공() {
        // Given
        Integer userId = user.getUserId();

        // 유저와 프로필 연관 설정
        profile.setUser(user);
        user.setProfile(profile);

        // 리뷰 모의 객체 설정
        Review review1 = mock(Review.class);
        Review review2 = mock(Review.class);
        when(review1.getStar()).thenReturn(4);
        when(review2.getStar()).thenReturn(5);

        // 리뷰 저장소와 유저 저장소의 반환값 설정
        when(reviewRepository.findByProfileOwnerUserId(userId)).thenReturn(
            List.of(review1, review2));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        profileService.updateProfileStar(userId);

        // Then
        assertEquals(4.5, profile.getStar()); // 평균 별점이 올바르게 계산되었는지 확인
        verify(reviewRepository).findByProfileOwnerUserId(userId); // 리뷰 조회 확인
        verify(userRepository).findById(userId); // 유저 조회 확인
    }

    @Test
    void 홈_데이터_조회_성공() {

        when(profileRepository.findByUser_Role(Role.KOREAN)).thenReturn(
            List.of(profile)); // setUp에서 설정한 profile을 사용

        // When
        HomeDataResponse homeDataResponse = profileService.getHomeDataProfiles();  // 홈 데이터 조회 메서드 호출

        // Then
        assertNotNull(homeDataResponse);
        assertEquals(1, homeDataResponse.getData().size());  // 반환된 데이터 개수 확인
        HomeProfileResponse response = homeDataResponse.getData().get(0);  // 첫 번째 데이터 검증
        assertEquals("Test User", response.getUsername());
        assertEquals("profileImg.png", response.getImgProf());
        assertEquals(4.5, response.getStar());
    }

    @Test
    void 프로필_DTO_조회_예외_프로필_비공개() {
        // Given
        Integer userId = user.getUserId();
        profile.setVisible(false); // 비공개 설정
        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profile));

        // When & Then
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            profileService.getProfileDTOByUserId(userId);
        });

        assertEquals("해당 프로필은 비공개 상태입니다.", exception.getMessage());
    }

    @Test
    void 프로필_이미지_업데이트_이미지_없음() {
        // Given
        Integer userId = user.getUserId();
        Profile profile = new Profile();

        // PrePersist에 의해 기본값이 설정된 상태를 Mock으로 반영
        profile.prePersist();

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(
            invocation -> invocation.getArgument(0));

        // When
        Profile updatedProfile = profileService.updateProfileImage(userId, null, null);

        // Then
        assertNotNull(updatedProfile); // 프로필 객체는 null이 아님
        assertEquals("/profile-image.png", updatedProfile.getImgProf()); // 기본값 검증
        assertEquals("/basic_background.png", updatedProfile.getImgBack()); // 기본값 검증
    }


    @Test
    void 프로필_정보_업데이트_해시태그_없음() {
        // Given
        Integer userId = 1;
        Profile profile = new Profile();
        profile.setUser(new User()); // User와 연결
        IndividualProfileResponse profileDto = IndividualProfileResponse.builder()
            .region("Seoul")
            .time("Morning")
            .description("Updated description")
            .hashtags(null) // 해시태그 없음
            .build();

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(
            invocation -> invocation.getArgument(0));

        // When
        Profile updatedProfile = profileService.updateProfile(userId, profileDto);

        // Then
        assertNotNull(updatedProfile);
        assertEquals("Seoul", updatedProfile.getRegion());
        assertEquals("Morning", updatedProfile.getTime());
        assertEquals("Updated description", updatedProfile.getDescription());
        assertTrue(updatedProfile.getMainCategories().isEmpty()); // 해시태그 리스트 비어있는지 확인
    }

    @Test
    void 프로필_별점_업데이트_리뷰_없음() {
        // Given
        Integer userId = user.getUserId();
        when(reviewRepository.findByProfileOwnerUserId(userId)).thenReturn(List.of()); // 리뷰 없음

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            profileService.updateProfileStar(userId);
        });

        assertEquals("해당 유저에 대한 리뷰가 없습니다.", exception.getMessage());
    }

    @Test
    void 홈_데이터_조회_데이터_없음() {
        when(profileRepository.findByUser_Role(Role.KOREAN)).thenReturn(List.of()); // 빈 리스트 반환

        // When
        HomeDataResponse homeDataResponse = profileService.getHomeDataProfiles();

        // Then
        assertNotNull(homeDataResponse);
        assertTrue(homeDataResponse.getData().isEmpty()); // 반환된 데이터가 비어있는지 확인
    }


}