package uni.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uni.backend.domain.Matching;
import uni.backend.domain.Matching.Status;
import uni.backend.domain.User;
import uni.backend.domain.dto.*;
import uni.backend.repository.MatchingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final MatchingRepository matchingRepository;
    private final UserService userService;

    @Transactional
    public MatchingCreateResponse createMatchRequest(MatchingCreateRequest request) {
        User requester = userService.findById(request.getRequesterId());
        User receiver = userService.findById(request.getReceiverId());

        Optional<Matching> existingMatching = findPendingRequest(request.getRequesterId(),
            request.getReceiverId());
        if (existingMatching.isPresent()) {
            return MatchingCreateResponse.from(existingMatching.get());
        }

        Matching newRequest = Matching.builder()
            .requester(requester)
            .receiver(receiver)
            .status(Matching.Status.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        Matching savedRequest = matchingRepository.save(newRequest);
        return MatchingCreateResponse.from(savedRequest);
    }

    @Transactional
    public String respondToMatchRequest(MatchingUpdateRequest request) {
        Optional<Matching> existingRequest = matchingRepository.findById(request.getMatchingId());
        if (existingRequest.isPresent()) {
            Matching match = existingRequest.get();
            match.setStatus(
                request.isAccepted() ? Matching.Status.ACCEPTED : Matching.Status.REJECTED);
            matchingRepository.save(match);

            return request.isAccepted() ? "매칭 요청이 수락되었습니다." : "매칭 요청이 거절되었습니다.";
        }
        return null;
    }

    @Transactional
    public MatchingUpdateResponse handleMatchResponse(MatchingUpdateRequest request) {
        Optional<Matching> existingRequest = matchingRepository.findById(request.getMatchingId());
        if (existingRequest.isPresent()) {
            Matching match = existingRequest.get();
            match.setStatus(
                request.isAccepted() ? Matching.Status.ACCEPTED : Matching.Status.REJECTED);
            matchingRepository.save(match);

            return MatchingUpdateResponse.from(match);
        }
        throw new IllegalArgumentException("Matching not found");
    }

    public String getMatchingResponseMessage(MatchingUpdateResponse response) {
        String responseMessage;
        if (response.getStatus().equals(Status.ACCEPTED)) {
            responseMessage = "매칭 요청이 수락되었습니다.";
        } else if (response.getStatus().equals(Status.REJECTED)) {
            responseMessage = "매칭 요청이 거절되었습니다.";
        } else {
            responseMessage = "매칭 상태가 변경되었습니다.";
        }
        return responseMessage;
    }

    public List<MatchingListResponse> getMatchingListByRequester(Integer requesterId) {
        return matchingRepository.findByRequester_UserId(requesterId).stream()
            .map(MatchingListResponse::fromMatching)
            .peek(response -> response.setUserName(
                userService.findById(response.getReceiverId()).getName()))
            .collect(Collectors.toList());
    }

    public List<MatchingListResponse> getMatchingListByReceiver(Integer receiverId) {
        return matchingRepository.findByReceiver_UserId(receiverId).stream()
            .map(MatchingListResponse::fromMatching)
            .peek(response -> response.setUserName(
                userService.findById(response.getRequesterId()).getName()))
            .collect(Collectors.toList());
    }

    public MatchingResponse getMatchingInfo(Integer matchingId) {
        Matching matching = matchingRepository.findById(matchingId)
            .orElseThrow(
                () -> new IllegalArgumentException("Matching not found with ID: " + matchingId));
        return new MatchingResponse(
            matching.getMatchingId(),
            matching.getReceiver().getUserId(),
            matching.getRequester().getUserId());
    }

    public MatchingCreateResponse getPendingMatching(Integer requesterId, Integer receiverId) {
        return findPendingRequest(requesterId, receiverId)
            .map(MatchingCreateResponse::from)
            .orElse(null);
    }

    private Optional<Matching> findPendingRequest(Integer requesterId, Integer receiverId) {
        return matchingRepository.findByRequester_UserIdAndReceiver_UserIdAndStatus(
            requesterId, receiverId, Matching.Status.PENDING);
    }
}