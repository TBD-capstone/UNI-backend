package uni.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uni.backend.domain.Matching;
import uni.backend.domain.User;
import uni.backend.domain.dto.MatchingResponse;
import uni.backend.repository.MatchingRepository;

import java.time.LocalDateTime;
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

        MatchingResponse response = new MatchingResponse();
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

        MatchingResponse response = new MatchingResponse();
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
        MatchingResponse response = new MatchingResponse();
        response.setRequestId(requestId);
        response.setAccepted(true);

        when(matchingRepository.findById(requestId)).thenReturn(Optional.empty());

        // when
        Optional<Matching> resultOpt = matchingService.updateRequestStatus(response);

        // then
        assertEquals(Optional.empty(), resultOpt);
        verify(matchingRepository, never()).save(any(Matching.class));
    }
}
