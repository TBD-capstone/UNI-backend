package uni.backend.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uni.backend.domain.Profile;
import uni.backend.domain.Role;
import uni.backend.domain.dto.HomeDataResponse;
import uni.backend.domain.dto.HomeProfileResponse;
import uni.backend.repository.ProfileRepository;
import uni.backend.util.MainCategoryMap;

@Service
public class HomeService {

    private final ProfileRepository profileRepository;

    public HomeService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public HomeProfileResponse profileToHomeProfileResponse(Profile profile) {
        HomeProfileResponse homeProfileResponse = new HomeProfileResponse();
        homeProfileResponse.setUsername(profile.getUser().getName());
        homeProfileResponse.setImgProf(profile.getImgProf());
        homeProfileResponse.setStar(profile.getStar());
        homeProfileResponse.setUnivName(profile.getUser().getUnivName());
        homeProfileResponse.setHashtags(profile.getHashtagStringList());
        homeProfileResponse.setUserId(profile.getUser().getUserId());

        return homeProfileResponse;
    }

    public Page<HomeProfileResponse> searchByUnivNameAndHashtags(
        String univName, List<String> hashtags, int page, String sortCriteria) {

        Sort sort;
        switch (sortCriteria) {
            case "highest_rating":
                sort = Sort.by(Sort.Direction.DESC, "star");
                break;
            case "lowest_rating":
                sort = Sort.by(Sort.Direction.ASC, "star");
                break;
            case "newest":
            default:
                sort = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
        }

        Pageable pageable = PageRequest.of(page, 10, sort);

        // hashtags가 null이 아니면 size 전달
        int hashtagsSize = hashtags != null ? hashtags.size() : 0;

        Page<Profile> profiles = profileRepository.findByUnivNameAndHashtags(univName, hashtags,
            hashtagsSize, pageable);
        return profiles.map(this::profileToHomeProfileResponse);
    }

    public void changeHashtagsToKorean(List<String> hashtags) {
        for (int i = 0; i < hashtags.size(); i++) {
            String originalTag = hashtags.get(i);
            String normalizedTag = originalTag.toLowerCase();
            String koreanTag = MainCategoryMap.HASHTAG_TRANSLATION_MAP.getOrDefault(normalizedTag,
                originalTag); // 매핑되지 않으면 원본 유지
            hashtags.set(i, koreanTag);
        }
    }

//    public HomeDataResponse searchByUnivNameAndHashtags(Pageable pageable) {
//        HomeDataResponse homeDataResponse = new HomeDataResponse();
//        List<Profile> list = profileRepository.findByUser_Role(Role.KOREAN);
//
//        homeDataResponse.setData(list.stream()
//            .map(HomeService::profileToHomeProfileResponse)
//            .collect(Collectors.toList()));
//
//        return homeDataResponse;
//    }


}
