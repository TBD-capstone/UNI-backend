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
            .requester(requester)
            .receiver(receiver)
            .status(Matching.Status.PENDING)
            .build();

        when(userService.findById(1)).thenReturn(requester);
        when(userService.findById(2)).thenReturn(receiver);
        when(matchingService.findPendingRequest(1, 2)).thenReturn(Optional.empty());
        when(matchingService.createRequest(any(Matching.class), eq(requester), eq(receiver)))
            .thenReturn(newMatching);

        // When
        ResponseEntity<MatchingCreateResponse> response = matchingController.createMatchRequest(
            request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getRequesterId());
        assertEquals(2, response.getBody().getReceiverId());
        verify(messagingTemplate, times(1))
            .convertAndSend("/sub/match-request/2", "매칭 요청이 도착했습니다.");
    }

    @Test
    void 매칭_응답_처리_요청_존재() {
        // Given
        MatchingUpdateRequest updateRequest = new MatchingUpdateRequest(1, true);

        User requester = new User();
        requester.setUserId(1);

        Matching matching = Matching.builder()
            .matchingId(1)
            .requester(requester)
            .receiver(new User())
            .status(Matching.Status.PENDING)
            .build();

        when(matchingService.updateRequestStatus(updateRequest)).thenReturn(Optional.of(matching));

        // When
        ResponseEntity<String> response = matchingController.respondToMatchRequest(updateRequest);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("응답 처리 완료", response.getBody());
        verify(messagingTemplate, times(1))
            .convertAndSend("/sub/match-response/1", "매칭 요청이 수락되었습니다.");
    }

    @Test
    void 요청자_아이디로_매칭_목록_조회() {
        // Given
        int requesterId = 1;
        User requester = new User();
        requester.setUserId(requesterId);

        User receiver1 = new User();
        receiver1.setUserId(2);
        receiver1.setName("Receiver1");

        User receiver2 = new User();
        receiver2.setUserId(3);
        receiver2.setName("Receiver2");

        Matching matching1 = Matching.builder()
            .matchingId(1)
            .requester(requester)
            .receiver(receiver1)
            .status(Matching.Status.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        Matching matching2 = Matching.builder()
            .matchingId(2)
            .requester(requester)
            .receiver(receiver2)
            .status(Matching.Status.ACCEPTED)
            .createdAt(LocalDateTime.now())
            .build();

        when(matchingService.getMatchingListByRequesterId(requesterId))
            .thenReturn(List.of(matching1, matching2));

        when(userService.findById(2)).thenReturn(receiver1);
        when(userService.findById(3)).thenReturn(receiver2);

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
        verify(matchingService, times(1)).getMatchingListByRequesterId(requesterId);
    }

    @Test
    void 대기중인_매칭_요청_존재시_성공적으로_반환() {
        // Given
        int requesterId = 1;
        int receiverId = 2;

        User requester = new User();
        requester.setUserId(requesterId);

        User receiver = new User();
        receiver.setUserId(receiverId);

        Matching matching = Matching.builder()
            .matchingId(1)
            .requester(requester) // 미리 생성된 User 객체 사용
            .receiver(receiver)  // 미리 생성된 User 객체 사용
            .status(Matching.Status.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        // Mock 설정: 대기 중인 요청이 존재
        when(matchingService.findPendingRequest(requesterId, receiverId))
            .thenReturn(Optional.of(matching));

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
        verify(matchingService, times(1)).findPendingRequest(requesterId, receiverId);
    }

    @Test
    void 대기중인_매칭_요청이_없을때_NoContent_응답() {
        // Given
        int requesterId = 1;
        int receiverId = 2;

        when(matchingService.findPendingRequest(requesterId, receiverId))
            .thenReturn(Optional.empty());

        // When
        ResponseEntity<MatchingCreateResponse> response = matchingController.getPendingMatching(
            requesterId, receiverId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(matchingService, times(1)).findPendingRequest(requesterId, receiverId);
    }

    @Test
    void 수신자_아이디로_매칭_목록_조회() {
        // Given
        int receiverId = 2;

        User requester1 = new User();
        requester1.setUserId(1);
        requester1.setName("Requester1");

        User requester2 = new User();
        requester2.setUserId(3);
        requester2.setName("Requester3");

        User receiver = new User();
        receiver.setUserId(receiverId);

        Matching matching1 = Matching.builder()
            .matchingId(1)
            .requester(requester1)
            .receiver(receiver)
            .status(Matching.Status.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        Matching matching2 = Matching.builder()
            .matchingId(2)
            .requester(requester2)
            .receiver(receiver)
            .status(Matching.Status.ACCEPTED)
            .createdAt(LocalDateTime.now())
            .build();

        when(matchingService.getMatchingListByReceiverId(receiverId))
            .thenReturn(List.of(matching1, matching2));
        when(userService.findById(1)).thenReturn(requester1); // 요청자 1 Mock 설정
        when(userService.findById(3)).thenReturn(requester2); // 요청자 2 Mock 설정

        // When
        List<MatchingListResponse> responses = matchingController.getMatchingListByReceiver(
            receiverId);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Requester1", responses.get(0).getUserName());
        assertEquals("Requester3", responses.get(1).getUserName());
        verify(matchingService, times(1)).getMatchingListByReceiverId(receiverId);
        verify(userService, times(1)).findById(1);
        verify(userService, times(1)).findById(3);
    }

    @Test
    void 매칭_ID로_매칭_정보_조회() {
        // Given
        int matchingId = 1;

        MatchingResponse matchingResponse = new MatchingResponse(1, 2, 3);

        when(matchingService.getMatchingInfo(matchingId)).thenReturn(matchingResponse);

        // When
        ResponseEntity<MatchingResponse> response = matchingController.getMatchingInfo(matchingId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getMatchingId());
        assertEquals(2, response.getBody().getProfileOwnerId());
        assertEquals(3, response.getBody().getRequesterId());
        verify(matchingService, times(1)).getMatchingInfo(matchingId);
    }

    @Test
    void 대기_중인_매칭_조회_존재하는_요청() {
        // Given
        int requesterId = 1;
        int receiverId = 2;

        User requester = new User();
        requester.setUserId(requesterId);

        User receiver = new User();
        receiver.setUserId(receiverId);

        Matching matching = Matching.builder()
            .matchingId(1)
            .requester(requester)
            .receiver(receiver)
            .status(Matching.Status.PENDING)
            .build();

        when(matchingService.findPendingRequest(requesterId, receiverId))
            .thenReturn(Optional.of(matching));

        // When
        ResponseEntity<MatchingCreateResponse> response = matchingController.getPendingMatching(
            requesterId, receiverId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getMatchingId());
        verify(matchingService, times(1)).findPendingRequest(requesterId, receiverId);
    }

    @Test
    void 대기_중인_매칭_조회_요청없음() {
        // Given
        int requesterId = 1;
        int receiverId = 2;

        when(matchingService.findPendingRequest(requesterId, receiverId))
            .thenReturn(Optional.empty());

        // When
        ResponseEntity<MatchingCreateResponse> response = matchingController.getPendingMatching(
            requesterId, receiverId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(matchingService, times(1)).findPendingRequest(requesterId, receiverId);
    }
}