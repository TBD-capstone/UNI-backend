package uni.backend.service;

import java.awt.print.Pageable;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static HomeProfileResponse profileToHomeProfileResponse(Profile profile) {
        HomeProfileResponse homeProfileResponse = new HomeProfileResponse();
        homeProfileResponse.setUsername(profile.getUser().getName());
        homeProfileResponse.setImgProf(profile.getImgProf());
        homeProfileResponse.setStar(profile.getStar());
        homeProfileResponse.setUnivName(profile.getUser().getUnivName());
        homeProfileResponse.setHashtags(profile.getHashtagStringList());
        homeProfileResponse.setUserId(profile.getUser().getUserId());

        return homeProfileResponse;
    }

    public HomeDataResponse getHomeDataProfiles(Pageable pageable) {
        HomeDataResponse homeDataResponse = new HomeDataResponse();
        List<Profile> list = profileRepository.findByUser_Role(Role.KOREAN);

        homeDataResponse.setData(list.stream()
            .map(HomeService::profileToHomeProfileResponse)
            .collect(Collectors.toList()));

        return homeDataResponse;
    }


}
