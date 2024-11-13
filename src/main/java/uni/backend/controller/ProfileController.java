package uni.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uni.backend.domain.Profile;
import uni.backend.domain.Qna;
import uni.backend.domain.User;
import uni.backend.domain.dto.IndividualProfileResponse;
import uni.backend.domain.dto.QnaResponse;
import uni.backend.domain.dto.QnaUserResponse;
import uni.backend.domain.dto.ReplyResponse;
import uni.backend.service.AwsS3Service;
import uni.backend.service.HashtagService;
import uni.backend.service.ProfileService;
import uni.backend.service.QnaService;
import uni.backend.service.ReplyService;
import uni.backend.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;
    private final HashtagService hashtagService;
    private final QnaService qnaService;
    private final ReplyService replyService;
    private final AwsS3Service awsS3Service;

    @PostMapping("/user/{userId}/update-profile")
    public ResponseEntity<IndividualProfileResponse> updateProfile(
        @PathVariable Integer userId,
        @RequestParam(required = false) MultipartFile profileImage,
        @RequestParam(required = false) MultipartFile backgroundImage) {

        // 기존 프로필 조회
        Profile profile = profileService.findProfileByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("프로필이 존재하지 않습니다. : " + userId));

        // 기존 프로필 이미지 URL
        String existingProfileImageUrl = profile.getImgProf();
        String existingBackgroundImageUrl = profile.getImgBack();

        String profileImageUrl = existingProfileImageUrl;
        if (profileImage != null) {
            profileImageUrl = awsS3Service.upload(profileImage, "profile", userId);
        }

        String backgroundImageUrl = existingBackgroundImageUrl;
        if (backgroundImage != null) {
            backgroundImageUrl = awsS3Service.upload(backgroundImage, "background", userId);
        }

        IndividualProfileResponse updatedProfile = new IndividualProfileResponse();
        updatedProfile.setUserId(userId);
        updatedProfile.setImgProf(profileImageUrl);
        updatedProfile.setImgBack(backgroundImageUrl);

        profileService.updateProfile(userId, updatedProfile);

        return ResponseEntity.ok(updatedProfile);
    }


    @GetMapping("/user/{user_id}")
    public ResponseEntity<IndividualProfileResponse> getUserProfile(
        @PathVariable("user_id") Integer userId) {
        User user = userService.findById(userId);
        IndividualProfileResponse individualProfileResponse = new IndividualProfileResponse();

        individualProfileResponse.setUserId(user.getUserId());
        individualProfileResponse.setUserName(user.getName());
        individualProfileResponse.setImgProf(user.getProfile().getImgProf());
        individualProfileResponse.setImgBack(user.getProfile().getImgBack());
        individualProfileResponse.setUniv(user.getUnivName());
        individualProfileResponse.setRegion(user.getProfile().getRegion());
        individualProfileResponse.setDescription(user.getProfile().getDescription());
        individualProfileResponse.setNumEmployment(user.getProfile().getNumEmployment());
        individualProfileResponse.setStar(user.getProfile().getStar());
        individualProfileResponse.setTime(user.getProfile().getTime());

        // 해시태그 목록을 String으로 설정
        List<String> hashtags = user.getProfile().getMainCategories().stream()
            .map(mainCategory -> mainCategory.getHashtag().getHashtagName())
            .collect(Collectors.toList());
        individualProfileResponse.setHashtags(hashtags);

        return ResponseEntity.ok(individualProfileResponse);
    }


    @PostMapping("/user/{userId}")
    public ResponseEntity<IndividualProfileResponse> updateUserProfile(@PathVariable String userId,
        @RequestBody IndividualProfileResponse profileDto) {
        Profile updatedProfile = profileService.updateProfile(Integer.valueOf(userId), profileDto);
        IndividualProfileResponse individualProfileResponse = profileService.getProfileDTOByUserId(
            Integer.valueOf(userId));
        return ResponseEntity.ok(individualProfileResponse);
    }

}
