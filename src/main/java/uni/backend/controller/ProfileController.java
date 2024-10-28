package uni.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.Profile;
import uni.backend.dto.ProfileUpdateRequest;
import uni.backend.service.ProfileService;

import java.util.Optional;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{userId}")
    public Optional<Profile> getProfile(@PathVariable Long userId) {
        return profileService.findProfileByUserId(userId);
    }

    @PutMapping("/{userId}")
    public Profile updateProfile(@PathVariable Long userId,
                                 @RequestBody ProfileUpdateRequest request) {
        return profileService.updateProfile(userId, request.getContent(), request.getHashtag());
    }
}

