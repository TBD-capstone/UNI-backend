package uni.backend.controller;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import uni.backend.domain.Matching;
import uni.backend.domain.User;
import uni.backend.domain.dto.*;
import uni.backend.service.MatchingService;
import uni.backend.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchingControllerTest {

    @InjectMocks
    private MatchingController matchingController;

    @Mock
    private MatchingService matchingService;

    @Mock
    private UserService userService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 매칭_요청_생성_중복되지_않은_요청() {
        // Given
        MatchingCreateRequest request = new MatchingCreateRequest(1, 2);

        User requester = new User();
        requester.setUserId(1);

        User receiver = new User();
        receiver.setUserId(2);

        Matching newMatching = Matching.builder()
            .matchingId(10)
            .requester(requester)
            .receiver(receiver)
            .status(Matching.Status.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        MatchingCreateResponse expectedResponse = MatchingCreateResponse.from(newMatching);

        when(matchingService.createMatchRequest(request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<MatchingCreateResponse> response = matchingController.createMatchRequest(
            request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(10, response.getBody().getMatchingId());
        assertEquals(1, response.getBody().getRequesterId());
        assertEquals(2, response.getBody().getReceiverId());
        assertEquals(Matching.Status.PENDING.name(), response.getBody().getStatus());
        verify(matchingService, times(1)).createMatchRequest(request);
        verify(messagingTemplate, times(1))
            .convertAndSend("/sub/match-request/2", "매칭 요청이 도착했습니다.");
    }

    @Test
    void 매칭_응답_처리_요청_존재() {
        // Given
        MatchingUpdateRequest updateRequest = new MatchingUpdateRequest();
        updateRequest.setMatchingId(1);
        updateRequest.setAccepted(true);

        User requester = new User();
        requester.setUserId(1);

        Matching matching = Matching.builder()
            .matchingId(1)
            .requester(requester)
            .receiver(new User())
            .status(Matching.Status.ACCEPTED)
            .build();

        when(matchingService.respondToMatchRequest(updateRequest)).thenReturn("매칭 요청이 수락되었습니다.");

        // When
        ResponseEntity<String> response = matchingController.respondToMatchRequest(updateRequest);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("응답 처리 완료", response.getBody());
        verify(matchingService, times(1)).respondToMatchRequest(updateRequest);
        verify(messagingTemplate, times(1))
            .convertAndSend("/sub/match-response/1", "매칭 요청이 수락되었습니다.");
    }

    @Test
    void 매칭_응답_처리_요청_없음() {
        // Given
        MatchingUpdateRequest updateRequest = new MatchingUpdateRequest();
        updateRequest.setMatchingId(1);
        updateRequest.setAccepted(true);

        // Mock 설정: 서비스가 null 반환
        when(matchingService.respondToMatchRequest(updateRequest)).thenReturn(null);

        // When
        ResponseEntity<String> response = matchingController.respondToMatchRequest(updateRequest);

        // Then
        assertNotNull(response); // 응답이 null이 아닌지 확인
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()); // 상태 코드가 NOT_FOUND인지 확인

        // Mock 호출 검증
        verify(matchingService, times(1)).respondToMatchRequest(updateRequest); // 서비스 호출 검증
        verify(messagingTemplate, never()).convertAndSend(anyString(),
            anyString()); // 메시지 전송이 호출되지 않음 검증
    }

    @Test
    void 요청자_아이디로_매칭_목록_조회() {
        // Given
        int requesterId = 1;

        MatchingListResponse response1 = MatchingListResponse.builder()
            .matchingId(1)
            .userName("Receiver1")
            .status(Matching.Status.PENDING.name())
            .build();

        MatchingListResponse response2 = MatchingListResponse.builder()
            .matchingId(2)
            .userName("Receiver2")
            .status(Matching.Status.ACCEPTED.name())
            .build();

        when(matchingService.getMatchingListByRequester(requesterId))
            .thenReturn(List.of(response1, response2));

        // When
        List<MatchingListResponse> responses = matchingController.getMatchingListByRequester(
            requesterId);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Receiver1", responses.get(0).getUserName());
        assertEquals("Receiver2", responses.get(1).getUserName());
        assertEquals(Matching.Status.PENDING.name(), responses.get(0).getStatus());
        assertEquals(Matching.Status.ACCEPTED.name(), responses.get(1).getStatus());
        verify(matchingService, times(1)).getMatchingListByRequester(requesterId); // 서비스 호출 검증
    }

    @Test
    void 대기중인_매칭_요청_존재시_성공적으로_반환() {
        // Given
        int requesterId = 1;
        int receiverId = 2;

        MatchingCreateResponse responseBody = MatchingCreateResponse.builder()
            .matchingId(1)
            .requesterId(requesterId)
            .receiverId(receiverId)
            .status(Matching.Status.PENDING.name())
            .build();

        // Mock 설정: 대기 중인 요청이 존재
        when(matchingService.getPendingMatching(requesterId, receiverId)).thenReturn(responseBody);

        // When
        ResponseEntity<MatchingCreateResponse> response = matchingController.getPendingMatching(
            requesterId, receiverId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode()); // 성공 상태 코드 확인
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getMatchingId()); // 매칭 ID 확인
        assertEquals(requesterId, response.getBody().getRequesterId()); // 요청자 ID 확인
        assertEquals(receiverId, response.getBody().getReceiverId()); // 수신자 ID 확인

        // Mock 호출 검증
        verify(matchingService, times(1)).getPendingMatching(requesterId, receiverId);
    }

    @Test
    void 대기중인_매칭_요청이_없을때_NoContent_응답() {
        // Given
        int requesterId = 1;
        int receiverId = 2;

        // Mock 설정: 대기 중인 요청이 없음
        when(matchingService.getPendingMatching(requesterId, receiverId)).thenReturn(null);

        // When
        ResponseEntity<MatchingCreateResponse> response = matchingController.getPendingMatching(
            requesterId, receiverId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode()); // 상태 코드가 NO_CONTENT인지 확인
        verify(matchingService, times(1)).getPendingMatching(requesterId, receiverId); // 서비스 호출 검증
    }

    @Test
    void 수신자_아이디로_매칭_목록_조회() {
        // Given
        int receiverId = 2;

        MatchingListResponse response1 = MatchingListResponse.builder()
            .matchingId(1)
            .userName("Requester1")
            .status(Matching.Status.PENDING.name())
            .build();

        MatchingListResponse response2 = MatchingListResponse.builder()
            .matchingId(2)
            .userName("Requester3")
            .status(Matching.Status.ACCEPTED.name())
            .build();

        // Mock 설정: 서비스가 반환하는 MatchingListResponse
        when(matchingService.getMatchingListByReceiver(receiverId)).thenReturn(
            List.of(response1, response2));

        // When
        List<MatchingListResponse> responses = matchingController.getMatchingListByReceiver(
            receiverId);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Requester1", responses.get(0).getUserName());
        assertEquals("Requester3", responses.get(1).getUserName());
        assertEquals(Matching.Status.PENDING.name(), responses.get(0).getStatus());
        assertEquals(Matching.Status.ACCEPTED.name(), responses.get(1).getStatus());
        verify(matchingService, times(1)).getMatchingListByReceiver(receiverId); // 서비스 호출 검증
    }

    @Test
    void 매칭_ID로_매칭_정보_조회() {
        // Given
        int matchingId = 1;

        MatchingResponse matchingResponse = new MatchingResponse(1, 2, 3);

        // Mock 설정: 서비스가 MatchingResponse 반환
        when(matchingService.getMatchingInfo(matchingId)).thenReturn(matchingResponse);

        // When
        ResponseEntity<MatchingResponse> response = matchingController.getMatchingInfo(matchingId);

        // Then
        assertNotNull(response); // 응답이 null이 아닌지 확인
        assertEquals(HttpStatus.OK, response.getStatusCode()); // 상태 코드가 OK인지 확인
        assertNotNull(response.getBody()); // 본문이 null이 아닌지 확인
        assertEquals(1, response.getBody().getMatchingId()); // 매칭 ID 검증
        assertEquals(2, response.getBody().getProfileOwnerId()); // 프로필 소유자 ID 검증
        assertEquals(3, response.getBody().getRequesterId()); // 요청자 ID 검증

        // 서비스 호출 검증
        verify(matchingService, times(1)).getMatchingInfo(matchingId);
    }

    @Test
    void 대기_중인_매칭_조회_존재하는_요청() {
        // Given
        int requesterId = 1;
        int receiverId = 2;

        MatchingCreateResponse matchingResponse = MatchingCreateResponse.builder()
            .matchingId(1)
            .requesterId(requesterId)
            .receiverId(receiverId)
            .status(Matching.Status.PENDING.name())
            .build();

        // Mock 설정: 대기 중인 매칭 요청이 존재
        when(matchingService.getPendingMatching(requesterId, receiverId)).thenReturn(
            matchingResponse);

        // When
        ResponseEntity<MatchingCreateResponse> response = matchingController.getPendingMatching(
            requesterId, receiverId);

        // Then
        assertNotNull(response); // 응답이 null이 아닌지 확인
        assertEquals(HttpStatus.OK, response.getStatusCode()); // 상태 코드가 OK인지 확인
        assertNotNull(response.getBody()); // 본문이 null이 아닌지 확인
        assertEquals(1, response.getBody().getMatchingId()); // 매칭 ID 확인
        assertEquals(requesterId, response.getBody().getRequesterId()); // 요청자 ID 확인
        assertEquals(receiverId, response.getBody().getReceiverId()); // 수신자 ID 확인
        assertEquals(Matching.Status.PENDING.name(), response.getBody().getStatus()); // 상태 확인

        // Mock 호출 검증
        verify(matchingService, times(1)).getPendingMatching(requesterId, receiverId);
    }

    @Test
    void 대기_중인_매칭_조회_요청없음() {
        // Given
        int requesterId = 1;
        int receiverId = 2;

        // Mock 설정: 대기 중인 매칭 요청 없음
        when(matchingService.getPendingMatching(requesterId, receiverId)).thenReturn(null);

        // When
        ResponseEntity<MatchingCreateResponse> response = matchingController.getPendingMatching(
            requesterId, receiverId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Mock 호출 검증
        verify(matchingService, times(1)).getPendingMatching(requesterId, receiverId);
    }

    @Test
    void handleMatchRequest_성공() {
        // Given
        MatchingCreateRequest createRequest = new MatchingCreateRequest(1, 2);

        MatchingCreateResponse createResponse = MatchingCreateResponse.builder()
            .matchingId(1)
            .requesterId(1)
            .receiverId(2)
            .status("PENDING")
            .build();

        when(matchingService.createMatchRequest(createRequest)).thenReturn(createResponse);

        // When
        MatchingCreateResponse response = matchingController.handleMatchRequest(createRequest);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getMatchingId());
        assertEquals(1, response.getRequesterId());
        assertEquals(2, response.getReceiverId());
        assertEquals("PENDING", response.getStatus());
        verify(matchingService, times(1)).createMatchRequest(createRequest); // 서비스 호출 검증
        verify(messagingTemplate, times(1))
            .convertAndSend("/sub/match-request/2", createResponse);
    }

    @Test
    void handleMatchResponse_성공() {
        // Given
        MatchingUpdateRequest updateRequest = new MatchingUpdateRequest();
        updateRequest.setMatchingId(1);
        updateRequest.setAccepted(true);

        MatchingUpdateResponse updateResponse = MatchingUpdateResponse.builder()
            .matchingId(1)
            .status("ACCEPTED")
            .build();

        when(matchingService.handleMatchResponse(updateRequest)).thenReturn(updateResponse);

        // When
        MatchingUpdateResponse response = matchingController.handleMatchResponse(updateRequest);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getMatchingId());
        assertEquals("ACCEPTED", response.getStatus());
        verify(matchingService, times(1)).handleMatchResponse(updateRequest);
        verify(messagingTemplate, times(1))
            .convertAndSend("/sub/match-response/" + response.getRequesterId(),
                matchingService.getMatchingResponseMessage(response));
    }
}
