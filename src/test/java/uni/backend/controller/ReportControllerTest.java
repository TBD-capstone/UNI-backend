package uni.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import uni.backend.domain.ReportCategory;
import uni.backend.domain.ReportReason;
import uni.backend.domain.dto.ReportRequest;
import uni.backend.service.ReportService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    private ReportRequest reportRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        reportRequest = ReportRequest.builder()
            .reason(ReportReason.INAPPROPRIATE_CONTENT)
            .category(ReportCategory.CHAT)
            .title("Inappropriate content")
            .detailedReason("The content is offensive and violates community guidelines.")
            .build();
    }

    @Test
    @DisplayName("특정 유저에 대한 신고 생성")
    void testCreateReport() {
        // given
        Integer userId = 1;

        // Prepare mocked response from the service
        Map<String, Object> mockedResponse = new HashMap<>();
        mockedResponse.put("message", "Report successfully created");
        mockedResponse.put("nextReportTime", "2024-12-09T00:00:00");

        when(reportService.createReport(userId, reportRequest)).thenReturn(mockedResponse);

        // when
        ResponseEntity<Map<String, Object>> response = reportController.createReport(userId,
            reportRequest);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Report successfully created",
            response.getBody().get("message"));
        assertEquals("2024-12-09T00:00:00",
            response.getBody().get("nextReportTime"));
        verify(reportService, times(1)).createReport(userId,
            reportRequest);
    }

}