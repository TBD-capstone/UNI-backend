package uni.backend.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Report;
import uni.backend.domain.User;
import uni.backend.domain.dto.ReportRequest;
import uni.backend.repository.ReportRepository;
import uni.backend.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

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

        // 신고당한 유저의 신고 횟수와 마지막 신고 사유 업데이트
        reportedUser.setReportCount(reportedUser.getReportCount() + 1);
        reportedUser.setLastReportReason(reportRequest.getDetailedReason());

        userRepository.save(reportedUser);
    }
}
