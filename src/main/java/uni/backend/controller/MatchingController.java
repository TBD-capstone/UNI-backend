package uni.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.Matching;
import uni.backend.domain.User;
import uni.backend.domain.dto.MatchingRequest;
import uni.backend.domain.dto.MatchingResponse;
import uni.backend.service.MatchingService;
import uni.backend.service.UserService;
import java.util.Optional;

@RestController
@RequestMapping("/api/match")
public class MatchingController {

    @Autowired
    private MatchingService matchingService;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/request")
    public MatchingRequest createMatchRequest(@RequestBody MatchingRequest matchingRequest) {
        User requester = userService.findById(matchingRequest.getRequesterId());
        User receiver = userService.findById(matchingRequest.getReceiverId());

        Matching request = Matching.builder()
                .requester(requester)
                .receiver(receiver)
                .status(Matching.Status.PENDING)
                .build();

        Matching savedRequest = matchingService.createRequest(request, requester, receiver);

        messagingTemplate.convertAndSend("/sub/match-request/" + matchingRequest.getReceiverId(), "매칭 요청이 도착했습니다.");

        matchingRequest.setRequestId(savedRequest.getRequestId());
        return matchingRequest;
    }

    @PostMapping("/respond")
    public String respondToMatchRequest(@RequestBody MatchingResponse matchingResponse) {
        Optional<Matching> requestOpt = matchingService.updateRequestStatus(matchingResponse);

        if (requestOpt.isPresent()) {
            Matching request = requestOpt.get();
            messagingTemplate.convertAndSend("/sub/match-response/" + request.getRequester().getUserId(),
                    matchingResponse.isAccepted() ? "매칭 요청이 수락되었습니다." : "매칭 요청이 거절되었습니다.");
            return "응답 처리 완료";
        }
        return "요청을 찾을 수 없습니다.";
    }

    @MessageMapping("/match/request")
    @SendTo("/sub/match/request")
    public MatchingRequest handleMatchRequest(MatchingRequest matchingRequest) {
        User requester = userService.findById(matchingRequest.getRequesterId());
        User receiver = userService.findById(matchingRequest.getReceiverId());

        Matching request = Matching.builder()
                .requester(requester)
                .receiver(receiver)
                .status(Matching.Status.PENDING)
                .build();

        Matching savedRequest = matchingService.createRequest(request, requester, receiver);

        messagingTemplate.convertAndSend("/sub/match-request/" + receiver.getUserId(), matchingRequest);

        matchingRequest.setRequestId(savedRequest.getRequestId());
        return matchingRequest;
    }

    @MessageMapping("/match/respond")
    @SendTo("/sub/match/response")
    public MatchingResponse handleMatchResponse(MatchingResponse matchingResponse) {
        Optional<Matching> requestOpt = matchingService.updateRequestStatus(matchingResponse);

        if (requestOpt.isPresent()) {
            Matching request = requestOpt.get();
            String responseMessage = matchingResponse.isAccepted() ? "매칭 요청이 수락되었습니다." : "매칭 요청이 거절되었습니다.";

            messagingTemplate.convertAndSend("/sub/match-response/" + request.getRequester().getUserId(), responseMessage);
        }

        return matchingResponse;
    }
}