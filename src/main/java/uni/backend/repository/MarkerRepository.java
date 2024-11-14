package uni.backend.repository;

import jakarta.persistence.criteria.CriteriaBuilder.In;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import uni.backend.domain.Marker;

public interface MarkerRepository extends JpaRepository<Marker, Integer> {

    // 특정 사용자의 마커 리스트를 조회하는 메서드
    List<Marker> findByUser_UserId(Integer userId);

    // 마커 ID로 마커 조회
    Optional<Marker> findById(Integer id);
}