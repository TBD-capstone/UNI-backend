package uni.backend.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uni.backend.domain.University;
import uni.backend.repository.UniversityRepository;

@Service
public class UniversityService {

  @Autowired
  private UniversityRepository universityRepository;

  public List<University> findAll() {
    return universityRepository.findAll(); // 데이터베이스에서 모든 대학 정보 조회
  }
}