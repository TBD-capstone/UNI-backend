// ProfileControllerTest.java
package uni.backend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uni.backend.controller.ProfileController;
import uni.backend.dto.ProfileUpdateRequest;
import uni.backend.domain.Profile;
import uni.backend.service.ProfileService;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileService profileService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenValidUserId_whenGetProfile_thenReturnsProfile() throws Exception {
        // Given
        Long userId = 1L;
        Profile profile = new Profile();
        profile.setId(userId);
        profile.setContent("Test content");
        profile.setHashtag("#test");

        when(profileService.findProfileByUserId(userId)).thenReturn(Optional.of(profile));

        // When & Then
        mockMvc.perform(get("/api/profiles/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.content").value("Test content"))
                .andExpect(jsonPath("$.hashtag").value("#test"));
    }

    @Test
    public void givenProfileUpdateRequest_whenUpdateProfile_thenReturnsUpdatedProfile() throws Exception {
        // Given
        Long userId = 1L;
        Profile profile = new Profile();
        profile.setId(userId);
        profile.setContent("Updated content");
        profile.setHashtag("#updated");

        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest();
        updateRequest.setContent("Updated content");
        updateRequest.setHashtag("#updated");

        when(profileService.updateProfile(userId, "Updated content", "#updated")).thenReturn(profile);

        // When & Then
        mockMvc.perform(put("/api/profiles/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.content").value("Updated content"))
                .andExpect(jsonPath("$.hashtag").value("#updated"));
    }
}
