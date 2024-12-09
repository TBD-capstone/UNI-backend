package uni.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uni.backend.domain.Report;
import uni.backend.domain.ReportCategory;
import uni.backend.domain.ReportReason;
import uni.backend.domain.User;
import uni.backend.domain.UserStatus;
import uni.backend.domain.dto.ReportRequest;
import uni.backend.repository.ReportRepository;
import uni.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("신고 생성 성공 테스트")
    void 신고_생성_성공() {
        // Given
        Integer reportedUserId = 1;
        Integer reporterUserId = 2;

        User reportedUser = new User();
        reportedUser.setUserId(reportedUserId);
        reportedUser.setReportCount(0L);

        User reporterUser = new User();
        reporterUser.setUserId(reporterUserId);

        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setReporterUserId(reporterUserId);
        reportRequest.setTitle("Harassment");
        reportRequest.setReason(ReportReason.SPAM);
        reportRequest.setDetailedReason("User sent offensive messages.");
        reportRequest.setCategory(ReportCategory.CHAT);

        when(userRepository.findById(reportedUserId)).thenReturn(Optional.of(reportedUser));
        when(userRepository.findById(reporterUserId)).thenReturn(Optional.of(reporterUser));

        // When
        Map<String, Object> response = reportService.createReport(reportedUserId, reportRequest);

        // Then
        assertEquals("신고가 성공적으로 접수되었습니다.", response.get("message"));
        assertEquals(1, reportedUser.getReportCount()); // 신고 횟수 증가 확인
        assertEquals("User sent offensive messages.", reportedUser.getLastReportReason());
        verify(reportRepository).save(any(Report.class)); // Report 저장 확인
        verify(userRepository).save(reportedUser); // 신고된 유저 저장 확인
    }


    @Test
    @DisplayName("중복 신고 제한 테스트")
    void 신고_중복_제한() {
        // Given
        User reporterUser = new User();
        reporterUser.setUserId(2);

        User reportedUser = new User();
        reportedUser.setUserId(1);

        Report lastReport = Report.builder()
            .reporterUser(reporterUser)
            .reportedUser(reportedUser)
            .reportedAt(LocalDateTime.now().minusHours(12)) // 12시간 전 신고
            .build();

        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setReporterUserId(2);
        reportRequest.setTitle("Spam");
        reportRequest.setDetailedReason("Repeated spam messages.");
        reportRequest.setReason(ReportReason.SPAM);
        reportRequest.setCategory(ReportCategory.CHAT);

        when(userRepository.findById(1)).thenReturn(Optional.of(reportedUser));
        when(userRepository.findById(2)).thenReturn(Optional.of(reporterUser));
        when(reportRepository.findFirstByReporterUserAndReportedUserOrderByReportedAtDesc(
            reporterUser, reportedUser))
            .thenReturn(lastReport);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            reportService.createReport(1, reportRequest)
        );

        assertEquals("같은 사용자에게 24시간 이내에 다시 신고할 수 없습니다.", exception.getMessage());
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("신고된 사용자 5회 이상 신고시 상태 변경 테스트")
    void 신고_5회_이상_밴_처리() {
        // Given
        User reportedUser = new User();
        reportedUser.setUserId(1);
        reportedUser.setReportCount(4L); // 이미 4회 신고된 상태

        User reporterUser = new User();
        reporterUser.setUserId(2);

        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setReporterUserId(2);
        reportRequest.setTitle("Abuse");
        reportRequest.setDetailedReason("Abusive behavior");
        reportRequest.setReason(ReportReason.SPAM);
        reportRequest.setCategory(ReportCategory.PROFILE);

        when(userRepository.findById(1)).thenReturn(Optional.of(reportedUser));
        when(userRepository.findById(2)).thenReturn(Optional.of(reporterUser));

        // When
        reportService.createReport(1, reportRequest);

        // Then
        assertEquals(5L, reportedUser.getReportCount());
        assertEquals(UserStatus.BANNED, reportedUser.getStatus());
        verify(adminService).blindAllContentByUser(1);
    }
}
