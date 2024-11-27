package uni.backend.service;

import java.util.List;
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

        // 정렬 조건 생성
        Sort sort;
        switch (sortCriteria) {
            case "highest_rating":
                sort = Sort.by(Sort.Direction.DESC, "star"); // 별점 높은 순
                break;
            case "lowest_rating":
                sort = Sort.by(Sort.Direction.ASC, "star"); // 별점 낮은 순
                break;
            case "newest":
            default:
                sort = Sort.by(Sort.Direction.DESC, "createdAt"); // 최신순
                break;
        }

        PageRequest pageable = PageRequest.of(page, 10, sort);

        Page<Profile> profiles = profileRepository.findByUnivNameAndHashtags(univName, hashtags,
            pageable);

        return profiles.map(this::profileToHomeProfileResponse);
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
