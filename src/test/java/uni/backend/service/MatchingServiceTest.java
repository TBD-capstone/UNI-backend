package uni.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uni.backend.domain.Matching;
import uni.backend.domain.User;
import uni.backend.domain.dto.MatchingCreateRequest;
import uni.backend.domain.dto.MatchingCreateResponse;
import uni.backend.domain.dto.MatchingListResponse;
import uni.backend.domain.dto.MatchingResponse;
import uni.backend.domain.dto.MatchingUpdateRequest;
import uni.backend.domain.dto.MatchingUpdateResponse;
import uni.backend.repository.MatchingRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MatchingServiceTest {

    @InjectMocks
    private MatchingService matchingService;

    @Mock
    private UserService userService;

    @Mock
    private MatchingRepository matchingRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 매칭_요청_생성() {
        // given
        MatchingCreateRequest request = new MatchingCreateRequest(1, 2);

        User requester = new User();
        requester.setUserId(1);

        User receiver = new User();
        receiver.setUserId(2);

        Matching expectedMatching = Matching.builder()
            .requester(requester)
            .receiver(receiver)
            .status(Matching.Status.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        MatchingCreateResponse expectedResponse = MatchingCreateResponse.from(expectedMatching);

        when(userService.findById(1)).thenReturn(requester);
        when(userService.findById(2)).thenReturn(receiver);
        when(matchingRepository.save(any(Matching.class))).thenReturn(expectedMatching);

        // when
        MatchingCreateResponse result = matchingService.createMatchRequest(request);

        // then
        assertEquals(1, result.getRequesterId());
        assertEquals(2, result.getReceiverId());
        assertEquals(Matching.Status.PENDING.name(), result.getStatus());
        verify(userService, times(1)).findById(1);
        verify(userService, times(1)).findById(2);
        verify(matchingRepository, times(1)).save(any(Matching.class));
    }

    @Test
    void 존재하는_매칭_요청_반환() {
        // given
        MatchingCreateRequest request = new MatchingCreateRequest(1, 2);

        User requester = new User();
        requester.setUserId(1);

        User receiver = new User();
        receiver.setUserId(2);

        Matching existingMatching = Matching.builder()
            .matchingId(10)
            .requester(requester)
            .receiver(receiver)
            .status(Matching.Status.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        // 기존 매칭 요청이 존재하는 경우를 Mock으로 설정
        when(userService.findById(1)).thenReturn(requester);
        when(userService.findById(2)).thenReturn(receiver);
        when(matchingRepository.findByRequester_UserIdAndReceiver_UserIdAndStatus(
            1, 2, Matching.Status.PENDING)).thenReturn(Optional.of(existingMatching));

        // when
        MatchingCreateResponse result = matchingService.createMatchRequest(request);

        // then
        // 기존 매칭 요청을 반환하는지 확인
        assertEquals(existingMatching.getMatchingId(), result.getMatchingId());
        assertEquals(existingMatching.getRequester().getUserId(), result.getRequesterId());
        assertEquals(existingMatching.getReceiver().getUserId(), result.getReceiverId());
        assertEquals(Matching.Status.PENDING.name(), result.getStatus());

        // Mock 검증
        verify(userService, times(1)).findById(1);
        verify(userService, times(1)).findById(2);
        verify(matchingRepository, never()).save(any(Matching.class)); // 새로운 매칭 저장이 호출되지 않아야 함
        verify(matchingRepository, times(1))
            .findByRequester_UserIdAndReceiver_UserIdAndStatus(1, 2, Matching.Status.PENDING);
    }

    @Test
    void 매칭_요청_수락() {
        // given
        int matchingId = 1;

        Matching matching = new Matching();
        matching.setMatchingId(matchingId);
        matching.setStatus(Matching.Status.PENDING);

        MatchingUpdateRequest request = new MatchingUpdateRequest();
        request.setMatchingId(matchingId);
        request.setAccepted(true);

        when(matchingRepository.findById(matchingId)).thenReturn(Optional.of(matching));

        // when
        String resultMessage = matchingService.respondToMatchRequest(request);

        // then
        assertEquals("매칭 요청이 수락되었습니다.", resultMessage);
        assertEquals(Matching.Status.ACCEPTED, matching.getStatus());
        verify(matchingRepository, times(1)).save(matching);
    }

    @Test
    void 매칭_요청_거절() {
        // given
        int matchingId = 2;

        Matching matching = new Matching();
        matching.setMatchingId(matchingId);
        matching.setStatus(Matching.Status.PENDING);

        MatchingUpdateRequest request = new MatchingUpdateRequest();
        request.setMatchingId(matchingId);
        request.setAccepted(false);

        when(matchingRepository.findById(matchingId)).thenReturn(Optional.of(matching));

        // when
        String resultMessage = matchingService.respondToMatchRequest(request);

        // then
        assertEquals("매칭 요청이 거절되었습니다.", resultMessage);
        assertEquals(Matching.Status.REJECTED, matching.getStatus());
        verify(matchingRepository, times(1)).save(matching);
    }

    @Test
    void 매칭_요청_찾을_수_없음() {
        // given
        int matchingId = 3;

        MatchingUpdateRequest request = new MatchingUpdateRequest();
        request.setMatchingId(matchingId);
        request.setAccepted(true);

        when(matchingRepository.findById(matchingId)).thenReturn(Optional.empty());

        // when
        String resultMessage = matchingService.respondToMatchRequest(request);

        // then
        assertNull(resultMessage); // 요청이 없으므로 null 반환 확인
        verify(matchingRepository, never()).save(any(Matching.class)); // save 호출되지 않음 검증
    }

    @Test
    void 매칭_응답_처리_성공() {
        // given
        int matchingId = 1;
        User requester = new User().builder()
            .userId(1)
            .build();

        Matching matching = new Matching();
        matching.setMatchingId(matchingId);
        matching.setRequester(requester);
        matching.setStatus(Matching.Status.PENDING);

        MatchingUpdateRequest request = new MatchingUpdateRequest();
        request.setMatchingId(matchingId);
        request.setAccepted(true); // 요청 수락

        when(matchingRepository.findById(matchingId)).thenReturn(Optional.of(matching));

        // when
        MatchingUpdateResponse response = matchingService.handleMatchResponse(request);

        // then
        assertEquals(Matching.Status.ACCEPTED.name(), response.getStatus());
        verify(matchingRepository, times(1)).findById(matchingId);
        verify(matchingRepository, times(1)).save(matching);
    }

    @Test
    void 매칭_응답_처리_거절() {
        // given
        int matchingId = 2;
        User requester = new User().builder()
            .userId(1)
            .build();

        Matching matching = new Matching();
        matching.setMatchingId(matchingId);
        matching.setRequester(requester);
        matching.setStatus(Matching.Status.PENDING);

        MatchingUpdateRequest request = new MatchingUpdateRequest();
        request.setMatchingId(matchingId);
        request.setAccepted(false); // 요청 거절

        when(matchingRepository.findById(matchingId)).thenReturn(Optional.of(matching));

        // when
        MatchingUpdateResponse response = matchingService.handleMatchResponse(request);

        // then
        assertEquals(Matching.Status.REJECTED.name(), response.getStatus());
        verify(matchingRepository, times(1)).findById(matchingId);
        verify(matchingRepository, times(1)).save(matching);
    }

    @Test
    void 매칭_응답_처리_실패_매칭_없음() {
        // given
        int matchingId = 999;

        MatchingUpdateRequest request = new MatchingUpdateRequest();
        request.setMatchingId(matchingId);
        request.setAccepted(true); // 요청 수락 시도

        when(matchingRepository.findById(matchingId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> matchingService.handleMatchResponse(request)
        );

        assertEquals("Matching not found", exception.getMessage());
        verify(matchingRepository, times(1)).findById(matchingId);
        verify(matchingRepository, never()).save(any(Matching.class)); // 저장이 호출되지 않음 확인
    }

    @Test
    void 요청자_ID로_매칭_목록_조회() {
        // given
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

        when(matchingRepository.findByRequester_UserId(requesterId))
            .thenReturn(Arrays.asList(matching1, matching2));
        when(userService.findById(2)).thenReturn(receiver1);
        when(userService.findById(3)).thenReturn(receiver2);

        // when
        List<MatchingListResponse> result = matchingService.getMatchingListByRequester(requesterId);

        // then
        assertEquals(2, result.size());
        assertEquals("Receiver1", result.get(0).getUserName());
        assertEquals("Receiver2", result.get(1).getUserName());
        assertEquals(Matching.Status.PENDING.name(), result.get(0).getStatus());
        assertEquals(Matching.Status.ACCEPTED.name(), result.get(1).getStatus());
        verify(matchingRepository, times(1)).findByRequester_UserId(requesterId);
        verify(userService, times(1)).findById(2);
        verify(userService, times(1)).findById(3);
    }

    @Test
    void 수신자_ID로_매칭_목록_조회() {
        // given
        int receiverId = 2;
        User receiver = new User();
        receiver.setUserId(receiverId);

        User requester1 = new User();
        requester1.setUserId(1);
        requester1.setName("Requester1");

        User requester2 = new User();
        requester2.setUserId(3);
        requester2.setName("Requester3");

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

        when(matchingRepository.findByReceiver_UserId(receiverId))
            .thenReturn(Arrays.asList(matching1, matching2));
        when(userService.findById(1)).thenReturn(requester1);
        when(userService.findById(3)).thenReturn(requester2);

        // when
        List<MatchingListResponse> result = matchingService.getMatchingListByReceiver(receiverId);

        // then
        assertEquals(2, result.size());
        assertEquals("Requester1", result.get(0).getUserName());
        assertEquals("Requester3", result.get(1).getUserName());
        assertEquals(Matching.Status.PENDING.name(), result.get(0).getStatus());
        assertEquals(Matching.Status.ACCEPTED.name(), result.get(1).getStatus());
        verify(matchingRepository, times(1)).findByReceiver_UserId(receiverId);
        verify(userService, times(1)).findById(1);
        verify(userService, times(1)).findById(3);
    }

    @Test
    void 매칭_정보_조회() {
        // given
        int matchingId = 1;
        User requester = new User();
        requester.setUserId(10);
        User receiver = new User();
        receiver.setUserId(20);

        Matching matching = Matching.builder()
            .matchingId(matchingId)
            .requester(requester)
            .receiver(receiver)
            .status(Matching.Status.ACCEPTED)
            .createdAt(LocalDateTime.now())
            .build();

        MatchingResponse expectedResponse = new MatchingResponse(matchingId, receiver.getUserId(),
            requester.getUserId());

        when(matchingRepository.findById(matchingId)).thenReturn(Optional.of(matching));

        // when
        MatchingResponse response = matchingService.getMatchingInfo(matchingId);

        // then
        assertEquals(expectedResponse.getMatchingId(), response.getMatchingId());
        assertEquals(expectedResponse.getProfileOwnerId(), response.getProfileOwnerId());
        assertEquals(expectedResponse.getRequesterId(), response.getRequesterId());
        verify(matchingRepository, times(1)).findById(matchingId);
    }

    @Test
    void 매칭_정보_조회_실패_매칭_ID_없음() {
        // given
        int matchingId = 999; // 존재하지 않는 ID
        when(matchingRepository.findById(matchingId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> matchingService.getMatchingInfo(matchingId)
        );

        // then
        assertEquals("Matching not found with ID: " + matchingId, exception.getMessage());
        verify(matchingRepository, times(1)).findById(matchingId);
    }

    @Test
    void 대기_중인_매칭_요청_조회() {
        // given
        int requesterId = 10;
        int receiverId = 20;

        User requester = new User();
        requester.setUserId(requesterId);
        User receiver = new User();
        receiver.setUserId(receiverId);

        Matching matching = Matching.builder()
            .matchingId(1)
            .requester(requester)
            .receiver(receiver)
            .status(Matching.Status.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        MatchingCreateResponse expectedResponse = MatchingCreateResponse.from(matching);

        when(matchingRepository.findByRequester_UserIdAndReceiver_UserIdAndStatus(
            requesterId, receiverId, Matching.Status.PENDING))
            .thenReturn(Optional.of(matching));

        // when
        MatchingCreateResponse result = matchingService.getPendingMatching(requesterId, receiverId);

        // then
        assertNotNull(result);
        assertEquals(expectedResponse.getMatchingId(), result.getMatchingId());
        assertEquals(expectedResponse.getRequesterId(), result.getRequesterId());
        assertEquals(expectedResponse.getReceiverId(), result.getReceiverId());
        assertEquals(expectedResponse.getStatus(), result.getStatus());
        verify(matchingRepository, times(1))
            .findByRequester_UserIdAndReceiver_UserIdAndStatus(
                requesterId, receiverId, Matching.Status.PENDING);
    }
}
