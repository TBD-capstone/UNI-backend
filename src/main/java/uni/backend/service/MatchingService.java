package uni.backend.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uni.backend.domain.Matching;
import uni.backend.domain.User;
import uni.backend.domain.dto.MatchingResponse;
import uni.backend.domain.dto.MatchingUpdateRequest;
import uni.backend.repository.MatchingRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MatchingService {

    @Autowired
    private MatchingRepository matchingRepository;

    @Transactional
    public Matching createRequest(Matching matching, User requester, User receiver) {
        Matching request = Matching.builder()
                .requester(requester)
                .receiver(receiver)
                .status(Matching.Status.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        return matchingRepository.save(request);
    }

    @Transactional
    public Optional<Matching> updateRequestStatus(MatchingUpdateRequest matchingUpdateRequest) {
        Optional<Matching> requestOpt = matchingRepository.findById(
                matchingUpdateRequest.getMatchingId());
        requestOpt.ifPresent(request -> {
            request.setStatus(matchingUpdateRequest.isAccepted() ? Matching.Status.ACCEPTED
                    : Matching.Status.REJECTED);
            matchingRepository.save(request);
        });
        return requestOpt;
    }

    public List<Matching> getMatchingListByRequesterId(Integer requesterId) {
        return matchingRepository.findByRequester_UserId(requesterId);
    }

    public List<Matching> getMatchingListByReceiverId(Integer receiverId) {
        return matchingRepository.findByReceiver_UserId(receiverId);
    }

    public MatchingResponse getMatchingInfo(Integer matchingId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Matching not found with ID: " + matchingId));

        Integer profileOwnerId = matching.getReceiver().getUserId(); // 리시버가 프로필 주인
        Integer requesterId = matching.getRequester().getUserId();

        return new MatchingResponse(matching.getMatchingId(), profileOwnerId, requesterId);
    }

    @Transactional
    public Optional<Matching> findPendingRequest(Integer requesterId, Integer receiverId) {
        return matchingRepository.findByRequester_UserIdAndReceiver_UserIdAndStatus(
                requesterId, receiverId, Matching.Status.PENDING);
    }

}