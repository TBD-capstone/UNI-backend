package uni.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uni.backend.domain.Report;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {

    // 특정 유저에 대한 모든 신고 리스트 조회
    List<Report> findByReportedUser_UserId(Integer reportedUserId);
}
