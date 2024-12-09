package uni.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.dto.*;
import uni.backend.service.MatchingService;

import java.util.List;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;
    private final SimpMessagingTemplate messagingTemplate;

    // 매칭 요청 생성 (REST API)
    @PostMapping("/request")
    public ResponseEntity<MatchingCreateResponse> createMatchRequest(
        @RequestBody MatchingCreateRequest request) {
        MatchingCreateResponse response = matchingService.createMatchRequest(request);

        messagingTemplate.convertAndSend("/sub/match-request/" + request.getReceiverId(),
            "매칭 요청이 도착했습니다.");
        return ResponseEntity.ok(response);
    }

    // 매칭 요청 생성 (Websocket)
    @MessageMapping("/match/request")
    @SendTo("/sub/match/request")
    public MatchingCreateResponse handleMatchRequest(MatchingCreateRequest request) {
        MatchingCreateResponse response = matchingService.createMatchRequest(request);

        messagingTemplate.convertAndSend("/sub/match-request/" + request.getReceiverId(), response);
        return response;
    }

    // 매칭 요청 응답 (REST API)
    @PostMapping("/respond")
    public ResponseEntity<String> respondToMatchRequest(
        @RequestBody MatchingUpdateRequest request) {
        String message = matchingService.respondToMatchRequest(request);
        if (message == null) {
            return ResponseEntity.notFound().build();
        }

        messagingTemplate.convertAndSend(
            "/sub/match-response/" + request.getMatchingId(), message);
        return ResponseEntity.ok("응답 처리 완료");
    }


    // 매칭 요청 응답 (WebSocket)
    @MessageMapping("/match/respond")
    @SendTo("/sub/match/response")
    public MatchingUpdateResponse handleMatchResponse(MatchingUpdateRequest request) {
        MatchingUpdateResponse response = matchingService.handleMatchResponse(request);

        messagingTemplate.convertAndSend("/sub/match-response/response", response);
        return response;
    }

    // 요청자 ID 로 매칭 목록 조회
    @GetMapping("/list/requester/{requesterId}")
    public List<MatchingListResponse> getMatchingListByRequester(
        @PathVariable Integer requesterId) {
        return matchingService.getMatchingListByRequester(requesterId);
    }

    // 수신자 ID 로 매칭 목록 조회
    @GetMapping("/list/receiver/{receiverId}")
    public List<MatchingListResponse> getMatchingListByReceiver(@PathVariable Integer receiverId) {
        return matchingService.getMatchingListByReceiver(receiverId);
    }

    // 매칭 ID 로 매칭 정보 조회
    @GetMapping("/{matchingId}")
    public ResponseEntity<MatchingResponse> getMatchingInfo(@PathVariable Integer matchingId) {
        return ResponseEntity.ok(matchingService.getMatchingInfo(matchingId));
    }

    // 대기 중인 매칭 요청 조회
    @GetMapping("/pending/{requesterId}/{receiverId}")
    public ResponseEntity<MatchingCreateResponse> getPendingMatching(
        @PathVariable Integer requesterId, @PathVariable Integer receiverId) {
        MatchingCreateResponse response = matchingService.getPendingMatching(requesterId,
            receiverId);
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.noContent().build();
    }
}