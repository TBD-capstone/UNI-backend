package uni.backend.service;

import java.util.HashMap;
import java.util.Map;
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
    private final AdminService adminService;

    @Transactional
    public Map<String, Object> createReport(Integer userId, ReportRequest reportRequest) {
        User reportedUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("신고된 유저를 찾을 수 없습니다. ID: " + userId));
        User reporterUser = userRepository.findById(reportRequest.getReporterUserId())
            .orElseThrow(() -> new IllegalArgumentException(
                "신고한 유저를 찾을 수 없습니다. ID: " + reportRequest.getReporterUserId()));

        // 중복 신고 제한 로직
        Report lastReport = reportRepository.findFirstByReporterUserAndReportedUserOrderByReportedAtDesc(
            reporterUser, reportedUser);
        if (lastReport != null && lastReport.getReportedAt()
            .isAfter(LocalDateTime.now().minusDays(1))) {
            throw new IllegalArgumentException("같은 사용자에게 24시간 이내에 다시 신고할 수 없습니다.");
        }

        // 새로운 신고 생성
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

        // 신고된 사용자 처리
        handleReportedUser(reportedUser, reportRequest.getDetailedReason());

        // 성공 메시지 반환
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("message", "신고가 성공적으로 접수되었습니다.");
        successResponse.put("nextReportAllowedAt", LocalDateTime.now().plusDays(1));
        return successResponse;
    }

    // 신고된 사용자 처리 메서드
    private void handleReportedUser(User reportedUser, String detailedReason) {
        reportedUser.setReportCount(reportedUser.getReportCount() + 1);
        reportedUser.setLastReportReason(detailedReason);

        if (reportedUser.getReportCount() >= 5) {
            reportedUser.setStatus(UserStatus.BANNED);
            adminService.blindAllContentByUser(reportedUser.getUserId());
        }

        // 유저 상태 저장
        userRepository.save(reportedUser);
    }

}
