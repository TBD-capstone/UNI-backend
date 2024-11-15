package uni.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.Matching;
import uni.backend.domain.User;
import uni.backend.domain.dto.*;
import uni.backend.service.MatchingService;
import uni.backend.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public MatchingCreateResponse createMatchRequest(@RequestBody MatchingCreateRequest matchingCreateRequest) {
        User requester = userService.findById(matchingCreateRequest.getRequesterId());
        User receiver = userService.findById(matchingCreateRequest.getReceiverId());

        Matching request = Matching.builder()
                .requester(requester)
                .receiver(receiver)
                .status(Matching.Status.PENDING)
                .build();

        Matching savedRequest = matchingService.createRequest(request, requester, receiver);

        messagingTemplate.convertAndSend("/sub/match-request/" + matchingCreateRequest.getReceiverId(), "매칭 요청이 도착했습니다.");

        return MatchingCreateResponse.from(savedRequest);
    }

    @PostMapping("/respond")
    public MatchingUpdateResponse respondToMatchRequest(@RequestBody MatchingUpdateRequest matchingUpdateRequest) {
        Optional<Matching> requestOpt = matchingService.updateRequestStatus(matchingUpdateRequest);

        if (requestOpt.isPresent()) {
            Matching request = requestOpt.get();
            String responseMessage = matchingUpdateRequest.isAccepted() ? "매칭 요청이 수락되었습니다." : "매칭 요청이 거절되었습니다.";
            messagingTemplate.convertAndSend("/sub/match-response/" + request.getRequester().getUserId(), responseMessage);

            return MatchingUpdateResponse.from(request);
        }
        throw new IllegalArgumentException("요청을 찾을 수 없습니다.");
    }

    @MessageMapping("/match/request")
    @SendTo("/sub/match/request")
    public MatchingCreateResponse handleMatchRequest(MatchingCreateRequest matchingCreateRequest) {

        User requester = userService.findById(matchingCreateRequest.getRequesterId());
        User receiver = userService.findById(matchingCreateRequest.getReceiverId());

        Matching request = Matching.builder()
                .requester(requester)
                .receiver(receiver)
                .status(Matching.Status.PENDING)
                .build();

        Matching savedRequest = matchingService.createRequest(request, requester, receiver);

        MatchingCreateResponse response = MatchingCreateResponse.from(savedRequest);

        messagingTemplate.convertAndSend("/sub/match-request/" + receiver.getUserId(), response);

        return response;
    }

    @MessageMapping("/match/respond")
    @SendTo("/sub/match/response")
    public MatchingUpdateResponse handleMatchResponse(MatchingUpdateRequest matchingUpdateRequest) {

        Optional<Matching> requestOpt = matchingService.updateRequestStatus(matchingUpdateRequest);

        if (requestOpt.isPresent()) {
            Matching request = requestOpt.get();
            String responseMessage = matchingUpdateRequest.isAccepted() ? "매칭 요청이 수락되었습니다." : "매칭 요청이 거절되었습니다.";

            messagingTemplate.convertAndSend("/sub/match-response/" + request.getRequester().getUserId(), responseMessage);

            return MatchingUpdateResponse.from(request);
        }

        throw new IllegalArgumentException("요청을 찾을 수 없습니다.");
    }


    @GetMapping("/list/requester/{requesterId}")
    public List<MatchingListResponse> getMatchingListByRequester(@PathVariable Integer requesterId) {
        return matchingService.getMatchingListByRequesterId(requesterId).stream()
                .map(MatchingListResponse::fromMatching)
                .collect(Collectors.toList());
    }

    @GetMapping("/list/receiver/{receiverId}")
    public List<MatchingListResponse> getMatchingListByReceiver(@PathVariable Integer receiverId) {
        return matchingService.getMatchingListByReceiverId(receiverId).stream()
                .map(MatchingListResponse::fromMatching)
                .collect(Collectors.toList());
    }
}
