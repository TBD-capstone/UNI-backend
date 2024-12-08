package uni.backend.repository;

import java.util.Optional;
import uni.backend.domain.University;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityRepository extends JpaRepository<University, Integer> {

    Optional<University> findByUniName(String univName);

    University findByEnUniName(String enUniName);
}

