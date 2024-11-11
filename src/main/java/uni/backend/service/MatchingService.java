package uni.backend.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uni.backend.domain.Matching;
import uni.backend.domain.User;
import uni.backend.domain.dto.MatchingResponse;
import uni.backend.repository.MatchingRepository;
import java.time.LocalDateTime;
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
    public Optional<Matching> updateRequestStatus(MatchingResponse matchingResponse) {
        Optional<Matching> requestOpt = matchingRepository.findById(matchingResponse.getRequestId());
        requestOpt.ifPresent(request -> {
            request.setStatus(matchingResponse.isAccepted() ? Matching.Status.ACCEPTED : Matching.Status.REJECTED);
            matchingRepository.save(request);
        });
        return requestOpt;
    }
}
