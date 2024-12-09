package uni.backend.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uni.backend.domain.University;
import uni.backend.domain.dto.UniversityResponse;
import uni.backend.repository.UniversityRepository;

@Service
public class UniversityService {

    @Autowired
    private UniversityRepository universityRepository;

    public List<University> findAll() {
        return universityRepository.findAll(); // 데이터베이스에서 모든 대학 정보 조회
    }

    public List<UniversityResponse> getUniversities() {
        return universityRepository.findAll().stream()
            .map(univ -> {
                UniversityResponse response = new UniversityResponse();
                response.setUniversityId(univ.getUniversityId());
                response.setUnivName(univ.getUniName());      // 한국어 이름
                response.setEnUnivName(univ.getEnUniName());  // 영어 이름
                return response;
            })
            .collect(Collectors.toList());
    }

    public String convertToKorean(String univName) {
        if (isEnglish(univName)) {
            University university = universityRepository.findByEnUniName(univName);
            if (university == null) {
                throw new IllegalArgumentException(
                    "University not found with English name: " + univName);
            }
            return university.getUniName();
        } else {
            return univName;
        }
    }

    public boolean isEnglish(String str) {
        return str.matches("^[a-zA-Z\\s]+$");
    }


}
