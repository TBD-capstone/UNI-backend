package uni.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<MatchingCreateResponse> createMatchRequest(
            @RequestBody MatchingCreateRequest matchingCreateRequest) {

        User requester = userService.findById(matchingCreateRequest.getRequesterId());
        User receiver = userService.findById(matchingCreateRequest.getReceiverId());

        // 중복 매칭 확인
        Optional<Matching> existingMatching = matchingService.findPendingRequest(requester.getUserId(), receiver.getUserId());
        if (existingMatching.isPresent()) {
            MatchingCreateResponse response = MatchingCreateResponse.from(existingMatching.get());
            return ResponseEntity.ok(response);
        }

        // 새로운 요청 생성
        Matching request = Matching.builder()
                .requester(requester)
                .receiver(receiver)
                .status(Matching.Status.PENDING)
                .build();

        Matching savedRequest = matchingService.createRequest(request, requester, receiver);

        MatchingCreateResponse response = MatchingCreateResponse.from(savedRequest);

        messagingTemplate.convertAndSend(
                "/sub/match-request/" + matchingCreateRequest.getReceiverId(), "매칭 요청이 도착했습니다.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/respond")
    public ResponseEntity<String> respondToMatchRequest(
        @RequestBody MatchingUpdateRequest matchingUpdateRequest) {
        Optional<Matching> requestOpt = matchingService.updateRequestStatus(matchingUpdateRequest);

        if (requestOpt.isPresent()) {
            Matching request = requestOpt.get();

            String responseMessage = matchingUpdateRequest.isAccepted()
                ? "매칭 요청이 수락되었습니다."
                : "매칭 요청이 거절되었습니다.";

            messagingTemplate.convertAndSend(
                "/sub/match-response/" + request.getRequester().getUserId(), responseMessage);

            return ResponseEntity.ok("응답 처리 완료");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("요청을 찾을 수 없습니다.");
    }


    @MessageMapping("/match/request")
    @SendTo("/sub/match/request")
    public MatchingCreateResponse handleMatchRequest(MatchingCreateRequest matchingCreateRequest) {

        User requester = userService.findById(matchingCreateRequest.getRequesterId());
        User receiver = userService.findById(matchingCreateRequest.getReceiverId());

        // 중복 매칭 확인
        Optional<Matching> existingMatching = matchingService.findPendingRequest(requester.getUserId(), receiver.getUserId());
        if (existingMatching.isPresent()) {
            return MatchingCreateResponse.from(existingMatching.get());
        }

        // 새로운 요청 생성
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
            String responseMessage =
                matchingUpdateRequest.isAccepted() ? "매칭 요청이 수락되었습니다." : "매칭 요청이 거절되었습니다.";

            messagingTemplate.convertAndSend(
                "/sub/match-response/" + request.getRequester().getUserId(), responseMessage);

            return MatchingUpdateResponse.from(request);
        }

        throw new IllegalArgumentException("요청을 찾을 수 없습니다.");
    }


    @GetMapping("/list/requester/{requesterId}")
    public List<MatchingListResponse> getMatchingListByRequester(
            @PathVariable Integer requesterId) {

        List<MatchingListResponse> matchingListResponses = matchingService.getMatchingListByRequesterId(requesterId).stream()
                .map(MatchingListResponse::fromMatching)
                .collect(Collectors.toList());

        for (MatchingListResponse matchingListResponse : matchingListResponses) {
            String receiverName = userService.findById(matchingListResponse.getReceiverId()).getName();
            matchingListResponse.setUserName(receiverName);
        }

        return matchingListResponses;
    }

    @GetMapping("/list/receiver/{receiverId}")
    public List<MatchingListResponse> getMatchingListByReceiver(
            @PathVariable Integer receiverId) {

        List<MatchingListResponse> matchingListResponses = matchingService.getMatchingListByReceiverId(receiverId).stream()
                .map(MatchingListResponse::fromMatching)
                .collect(Collectors.toList());

        for (MatchingListResponse matchingListResponse : matchingListResponses) {
            String requesterName = userService.findById(matchingListResponse.getRequesterId()).getName();
            matchingListResponse.setUserName(requesterName);
        }

        return matchingListResponses;
    }

    @GetMapping("/{matchingId}")
    public ResponseEntity<MatchingResponse> getMatchingInfo(@PathVariable Integer matchingId) {
        MatchingResponse response = matchingService.getMatchingInfo(matchingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending/{requesterId}/{receiverId}")
    public ResponseEntity<MatchingCreateResponse> getPendingMatching(
            @PathVariable Integer requesterId, @PathVariable Integer receiverId) {

        // 대기 중인 요청 확인
        Optional<Matching> existingMatching = matchingService.findPendingRequest(requesterId, receiverId);
        if (existingMatching.isPresent()) {
            MatchingCreateResponse response = MatchingCreateResponse.from(existingMatching.get());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.noContent().build(); // 대기 중인 요청이 없으면 No Content 응답
    }
}
