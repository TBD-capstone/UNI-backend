package uni.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uni.backend.domain.Matching;
import uni.backend.domain.User;
import uni.backend.domain.dto.MatchingUpdateRequest;
import uni.backend.repository.MatchingRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        int requestId = 1;
        Matching matching = new Matching();
        matching.setStatus(Matching.Status.PENDING);

        MatchingUpdateRequest response = new MatchingUpdateRequest();
        response.setRequestId(requestId);
        response.setAccepted(true);

        when(matchingRepository.findById(requestId)).thenReturn(Optional.of(matching));

        // when
        Optional<Matching> resultOpt = matchingService.updateRequestStatus(response);

        // then
        assertEquals(Matching.Status.ACCEPTED, resultOpt.get().getStatus());
        verify(matchingRepository, times(1)).save(matching);
    }

    @Test
    void 매칭_요청_거절() {
        // given
        int requestId = 2;
        Matching matching = new Matching();
        matching.setStatus(Matching.Status.PENDING);

        MatchingUpdateRequest response = new MatchingUpdateRequest();
        response.setRequestId(requestId);
        response.setAccepted(false);

        when(matchingRepository.findById(requestId)).thenReturn(Optional.of(matching));

        // when
        Optional<Matching> resultOpt = matchingService.updateRequestStatus(response);

        // then
        assertEquals(Matching.Status.REJECTED, resultOpt.get().getStatus());
        verify(matchingRepository, times(1)).save(matching);
    }

    @Test
    void 매칭_요청_찾을_수_없음() {
        // given
        int requestId = 3;
        MatchingUpdateRequest response = new MatchingUpdateRequest();
        response.setRequestId(requestId);
        response.setAccepted(true);

        when(matchingRepository.findById(requestId)).thenReturn(Optional.empty());

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
                .requestId(1)
                .requester(requester)
                .receiver(new User())
                .status(Matching.Status.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Matching matching2 = Matching.builder()
                .requestId(2)
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
                .requestId(1)
                .requester(new User())
                .receiver(receiver)
                .status(Matching.Status.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Matching matching2 = Matching.builder()
                .requestId(2)
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
}
