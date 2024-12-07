package uni.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uni.backend.domain.Matching;
import uni.backend.domain.User;
import uni.backend.domain.dto.MatchingResponse;
import uni.backend.domain.dto.MatchingUpdateRequest;
import uni.backend.repository.MatchingRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MatchingServiceTest {

    @InjectMocks
    private MatchingService matchingService;

    @Mock
    private MatchingRepository matchingRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 매칭_요청_생성() {
        // given
        User requester = new User();
        requester.setUserId(1);
        User receiver = new User();
        receiver.setUserId(2);

        Matching expectedMatching = new Matching();
        expectedMatching.setRequester(requester);
        expectedMatching.setReceiver(receiver);
        expectedMatching.setStatus(Matching.Status.PENDING);
        expectedMatching.setCreatedAt(LocalDateTime.now());

        when(matchingRepository.save(any(Matching.class))).thenReturn(expectedMatching);

        // when
        Matching result = matchingService.createRequest(expectedMatching, requester, receiver);

        // then
        assertEquals(Matching.Status.PENDING, result.getStatus());
        assertEquals(requester.getUserId(), result.getRequester().getUserId());
        assertEquals(receiver.getUserId(), result.getReceiver().getUserId());
        verify(matchingRepository, times(1)).save(any(Matching.class));
    }

    @Test
    void 매칭_요청_수락() {
        // given
        int matchingId = 1;
        Matching matching = new Matching();
        matching.setStatus(Matching.Status.PENDING);

        MatchingUpdateRequest response = new MatchingUpdateRequest();
        response.setMatchingId(matchingId);
        response.setAccepted(true);

        when(matchingRepository.findById(matchingId)).thenReturn(Optional.of(matching));

        // when
        Optional<Matching> resultOpt = matchingService.updateRequestStatus(response);

        // then
        assertEquals(Matching.Status.ACCEPTED, resultOpt.get().getStatus());
        verify(matchingRepository, times(1)).save(matching);
    }

    @Test
    void 매칭_요청_거절() {
        // given
        int matchingId = 2;
        Matching matching = new Matching();
        matching.setStatus(Matching.Status.PENDING);

        MatchingUpdateRequest response = new MatchingUpdateRequest();
        response.setMatchingId(matchingId);
        response.setAccepted(false);

        when(matchingRepository.findById(matchingId)).thenReturn(Optional.of(matching));

        // when
        Optional<Matching> resultOpt = matchingService.updateRequestStatus(response);

        // then
        assertEquals(Matching.Status.REJECTED, resultOpt.get().getStatus());
        verify(matchingRepository, times(1)).save(matching);
    }

    @Test
    void 매칭_요청_찾을_수_없음() {
        // given
        int matchingId = 3;
        MatchingUpdateRequest response = new MatchingUpdateRequest();
        response.setMatchingId(matchingId);
        response.setAccepted(true);

        when(matchingRepository.findById(matchingId)).thenReturn(Optional.empty());

        // when
        Optional<Matching> resultOpt = matchingService.updateRequestStatus(response);

        // then
        assertEquals(Optional.empty(), resultOpt);
        verify(matchingRepository, never()).save(any(Matching.class));
    }

    @Test
    void 요청자_ID로_매칭_목록_조회() {
        // given
        int requesterId = 1;
        User requester = new User();
        requester.setUserId(requesterId);

        Matching matching1 = Matching.builder()
            .matchingId(1)
            .requester(requester)
            .receiver(new User())
            .status(Matching.Status.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        Matching matching2 = Matching.builder()
            .matchingId(2)
            .requester(requester)
            .receiver(new User())
            .status(Matching.Status.ACCEPTED)
            .createdAt(LocalDateTime.now())
            .build();

        when(matchingRepository.findByRequester_UserId(requesterId))
            .thenReturn(Arrays.asList(matching1, matching2));

        // when
        List<Matching> result = matchingService.getMatchingListByRequesterId(requesterId);

        // then
        assertEquals(2, result.size());
        assertEquals(requesterId, result.get(0).getRequester().getUserId());
        assertEquals(requesterId, result.get(1).getRequester().getUserId());
        verify(matchingRepository, times(1)).findByRequester_UserId(requesterId);
    }

    @Test
    void 수신자_ID로_매칭_목록_조회() {
        // given
        int receiverId = 2;
        User receiver = new User();
        receiver.setUserId(receiverId);

        Matching matching1 = Matching.builder()
            .matchingId(1)
            .requester(new User())
            .receiver(receiver)
            .status(Matching.Status.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        Matching matching2 = Matching.builder()
            .matchingId(2)
            .requester(new User())
            .receiver(receiver)
            .status(Matching.Status.ACCEPTED)
            .createdAt(LocalDateTime.now())
            .build();

        when(matchingRepository.findByReceiver_UserId(receiverId))
            .thenReturn(Arrays.asList(matching1, matching2));

        // when
        List<Matching> result = matchingService.getMatchingListByReceiverId(receiverId);

        // then
        assertEquals(2, result.size());
        assertEquals(receiverId, result.get(0).getReceiver().getUserId());
        assertEquals(receiverId, result.get(1).getReceiver().getUserId());
        verify(matchingRepository, times(1)).findByReceiver_UserId(receiverId);
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

        when(matchingRepository.findById(matchingId)).thenReturn(Optional.of(matching));

        // when
        MatchingResponse response = matchingService.getMatchingInfo(matchingId);

        // then
        assertEquals(matchingId, response.getMatchingId());
        assertEquals(20, response.getProfileOwnerId()); // 리시버가 프로필 주인
        assertEquals(10, response.getRequesterId());
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

        assertEquals("Matching not found with ID: " + matchingId, exception.getMessage());
        verify(matchingRepository, times(1)).findById(matchingId);
    }

    @Test
    void 대기_중인_매칭_요청_조회() {
        // given
        int requesterId = 10;
        int receiverId = 20;

        User requester = new User();
        requester.setUserId(requesterId); // ID 설정
        User receiver = new User();
        receiver.setUserId(receiverId); // ID 설정

        Matching matching = Matching.builder()
            .matchingId(1)
            .requester(requester) // 직접 생성된 User 사용
            .receiver(receiver)  // 직접 생성된 User 사용
            .status(Matching.Status.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        when(matchingRepository.findByRequester_UserIdAndReceiver_UserIdAndStatus(
            requesterId, receiverId, Matching.Status.PENDING))
            .thenReturn(Optional.of(matching));

        // when
        Optional<Matching> result = matchingService.findPendingRequest(requesterId, receiverId);

        // then
        assertTrue(result.isPresent());
        assertEquals(Matching.Status.PENDING, result.get().getStatus());
        assertEquals(requesterId, result.get().getRequester().getUserId());
        assertEquals(receiverId, result.get().getReceiver().getUserId());
        verify(matchingRepository, times(1))
            .findByRequester_UserIdAndReceiver_UserIdAndStatus(
                requesterId, receiverId, Matching.Status.PENDING);
    }
}
