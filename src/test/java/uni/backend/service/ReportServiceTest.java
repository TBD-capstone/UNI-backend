package uni.backend.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
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
public class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private ReportService reportService;

    @Test
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
        reportRequest.setTitle("테스트 작성중 입니다.");
        reportRequest.setReason(ReportReason.SPAM);
        reportRequest.setDetailedReason("테스트 신고 중입니다. 테스트 테스트");
        reportRequest.setCategory(ReportCategory.CHAT);

        when(userRepository.findById(reportedUserId)).thenReturn(Optional.of(reportedUser));
        when(userRepository.findById(reporterUserId)).thenReturn(Optional.of(reporterUser));

        // When
        reportService.createReport(reportedUserId, reportRequest);

        // Then
        assertEquals(1, reportedUser.getReportCount()); // 신고 횟수 증가 확인
        assertEquals("테스트 신고 중입니다. 테스트 테스트", reportedUser.getLastReportReason());
        verify(reportRepository, times(1)).save(any(Report.class)); // Report 저장 확인
        verify(userRepository, times(1)).save(reportedUser); // 신고된 유저 저장 확인
        verifyNoInteractions(adminService); // AdminService 호출되지 않음
    }

    @Test
    void 신고_생성_블라인드_처리() {
        // Given
        Integer reportedUserId = 1;
        Integer reporterUserId = 2;

        User reportedUser = new User();
        reportedUser.setUserId(reportedUserId);
        reportedUser.setReportCount(4L); // 기존 신고 횟수

        User reporterUser = new User();
        reporterUser.setUserId(reporterUserId);

        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setReporterUserId(reporterUserId);
        reportRequest.setTitle("Harassment");
        reportRequest.setReason(ReportReason.SPAM);
        reportRequest.setDetailedReason("User sent multiple offensive messages.");
        reportRequest.setCategory(ReportCategory.CHAT);

        when(userRepository.findById(reportedUserId)).thenReturn(Optional.of(reportedUser));
        when(userRepository.findById(reporterUserId)).thenReturn(Optional.of(reporterUser));

        // When
        reportService.createReport(reportedUserId, reportRequest);

        // Then
        assertEquals(UserStatus.BANNED, reportedUser.getStatus()); // 유저 상태가 BANNED인지 확인
        assertEquals(5, reportedUser.getReportCount()); // 신고 횟수 증가 확인
        verify(reportRepository, times(1)).save(any(Report.class)); // Report 저장 확인
        verify(userRepository, times(1)).save(reportedUser); // 상태 업데이트 저장 확인
        verify(adminService, times(1)).blindAllContentByUser(reportedUserId); // 블라인드 처리 호출 확인
    }

    @Test
    void 신고_대상_유저_없음() {
        // Given
        Integer reportedUserId = 1;
        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setReporterUserId(2);

        when(userRepository.findById(reportedUserId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reportService.createReport(reportedUserId, reportRequest);
        });

        assertEquals("신고된 유저를 찾을 수 없습니다. ID: " + reportedUserId, exception.getMessage());
    }

    @Test
    void 신고_작성자_유저_없음() {
        // Given
        Integer reportedUserId = 1;
        Integer reporterUserId = 2;

        User reportedUser = new User();
        reportedUser.setUserId(reportedUserId);

        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setReporterUserId(reporterUserId);

        when(userRepository.findById(reportedUserId)).thenReturn(Optional.of(reportedUser));
        when(userRepository.findById(reporterUserId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reportService.createReport(reportedUserId, reportRequest);
        });

        assertEquals("신고한 유저를 찾을 수 없습니다. ID: " + reporterUserId, exception.getMessage());
    }

    @Test
    @DisplayName("중복 신고 제한 테스트 - 24시간 이내 중복 신고 불가")
    void createReport_duplicateWithin24Hours() {
        // Given
        User reporter = new User();
        reporter.setUserId(1);

        User reported = new User();
        reported.setUserId(2);

        Report lastReport = Report.builder()
            .reporterUser(reporter)
            .reportedUser(reported)
            .reportedAt(LocalDateTime.now().minusHours(12)) // 12시간 전 신고
            .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(2)).thenReturn(Optional.of(reported));
        when(reportRepository.findFirstByReporterUserAndReportedUserOrderByReportedAtDesc(reporter,
            reported))
            .thenReturn(lastReport);

        // When & Then
        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setReporterUserId(1);
        reportRequest.setDetailedReason("Test Reason");

        assertThrows(IllegalArgumentException.class,
            () -> reportService.createReport(2, reportRequest),
            "같은 사용자에게 24시간 이내에 다시 신고할 수 없습니다.");
    }
}
