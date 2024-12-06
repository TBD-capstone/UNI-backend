package uni.backend.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Report;
import uni.backend.domain.User;
import uni.backend.domain.UserStatus;
import uni.backend.domain.dto.ReportRequest;
import uni.backend.repository.ReportRepository;
import uni.backend.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AdminService adminService; // AdminService 주입

    @Transactional
    public void createReport(Integer userId, ReportRequest reportRequest) {
        User reportedUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("신고된 유저를 찾을 수 없습니다. ID: " + userId));
        User reporterUser = userRepository.findById(reportRequest.getReporterUserId())
            .orElseThrow(() -> new IllegalArgumentException(
                "신고한 유저를 찾을 수 없습니다. ID: " + reportRequest.getReporterUserId()));

        Report report = Report.builder()
            .title(reportRequest.getTitle())
            .reportedUser(reportedUser)
            .reporterUser(reporterUser)
            .reason(reportRequest.getReason())
            .detailedReason(reportRequest.getDetailedReason())
            .category(reportRequest.getCategory())
            .reportedAt(LocalDateTime.now())
            .build();

        reportRepository.save(report);

        reportedUser.setReportCount(reportedUser.getReportCount() + 1);
        reportedUser.setLastReportReason(reportRequest.getDetailedReason());

        if (reportedUser.getReportCount() >= 5) {
            reportedUser.setStatus(UserStatus.BANNED); // 상태 변경
            adminService.blindAllContentByUser(userId); // 블라인드 처리 호출
        }

        userRepository.save(reportedUser);
    }
}
