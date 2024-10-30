package uni.backend.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uni.backend.domain.Profile;
import uni.backend.domain.User;
import uni.backend.domain.dto.ProfileDTO;
import uni.backend.repository.ProfileRepository;

import java.time.LocalDateTime;
import java.util.Optional;

class ProfileServiceTest {

    @InjectMocks
    private ProfileService profileService;

    @Mock
    private ProfileRepository profileRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetProfileDTOByUserId() {
        // Given
        Integer userId = 1;
        Profile profile = new Profile();
        profile.setImgProf("profileImage.png");
        profile.setImgBack("backgroundImage.png");
        profile.setRegion("Seoul");
        profile.setExplain("About me");
        profile.setNumEmployment(5);
        profile.setStar(4.5);
        profile.setCreatedAt(LocalDateTime.now());

        // Profile 객체에 User 정보 추가
        User user = new User();
        user.setUniv("My University");
        profile.setUser(user);

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profile));

        // When
        ProfileDTO profileDTO = profileService.getProfileDTOByUserId(userId);

        // Then
        assertNotNull(profileDTO);
        assertEquals(userId, profileDTO.getUserId());
        assertEquals("profileImage.png", profileDTO.getImgProf());
        assertEquals("backgroundImage.png", profileDTO.getImgBack());
        assertEquals("My University", profileDTO.getUniv());
        assertEquals("Seoul", profileDTO.getRegion());
        assertEquals("About me", profileDTO.getExplain());
        assertEquals(5, profileDTO.getNumEmployment());
        assertEquals(4.5, profileDTO.getStar());
    }

    @Test
    void testGetProfileDTOByUserId_ThrowsException_WhenProfileNotFound() {
        // Given
        Integer userId = 1;

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            profileService.getProfileDTOByUserId(userId);
        });

        assertEquals("프로필이 존재하지 않습니다. : " + userId, exception.getMessage());
    }
}
