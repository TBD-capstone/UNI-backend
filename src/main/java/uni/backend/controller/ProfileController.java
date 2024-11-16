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

        // 이미지가 있다면 새로 업로드하고, 없다면 기존 이미지 URL을 사용
        String profileImageUrl = profileImage != null
            ? awsS3Service.upload(profileImage, "profile", userId)
            : existingProfileImageUrl;

        String backgroundImageUrl = backgroundImage != null
            ? awsS3Service.upload(backgroundImage, "background", userId)
            : existingBackgroundImageUrl;

        // 새로운 프로필 객체에 기존 프로필 값들을 유지하면서 수정된 정보 반영
        IndividualProfileResponse updatedProfile = new IndividualProfileResponse();
        updatedProfile.setUserId(userId);
        updatedProfile.setImgProf(profileImageUrl);  // 프로필 이미지 수정
        updatedProfile.setImgBack(backgroundImageUrl);  // 배경 이미지 수정

        // 기존 프로필의 나머지 정보는 그대로 유지
        updatedProfile.setUserName(profile.getUser().getName());
        updatedProfile.setUniv(profile.getUser().getUnivName());
        updatedProfile.setRegion(profile.getRegion());
        updatedProfile.setDescription(profile.getDescription());
        updatedProfile.setNumEmployment(profile.getNumEmployment());
        updatedProfile.setStar(profile.getStar());
        updatedProfile.setTime(profile.getTime());

        // 해시태그 목록을 String으로 설정
        List<String> hashtags = profile.getMainCategories().stream()
            .map(mainCategory -> mainCategory.getHashtag().getHashtagName())
            .collect(Collectors.toList());
        updatedProfile.setHashtags(hashtags);

        // 프로필 업데이트
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
