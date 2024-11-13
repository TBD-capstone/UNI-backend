package uni.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.Reply;
import uni.backend.domain.User;
import uni.backend.domain.dto.ReplyCreateRequest;
import uni.backend.domain.dto.ReplyCreateResponse;
import uni.backend.domain.dto.ReplyResponse;
import uni.backend.domain.dto.Response;
import uni.backend.repository.UserRepository;
import uni.backend.service.ReplyService;
import uni.backend.service.UserService;

@RestController
@RequestMapping("/api")
public class ReplyController {

    private final ReplyService replyService;
    private final UserRepository userRepository;
    private final UserService userService;

    public ReplyController(ReplyService replyService, UserRepository userRepository,
        UserService userService) {
        this.replyService = replyService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    // 특정 Qna에 대댓글 작성
    @PostMapping("/user/{userId}/qnas/{qnaId}/replies/{commenterId}")
    public ResponseEntity<ReplyCreateResponse> createReply(
        @PathVariable Integer userId,
        @PathVariable Integer qnaId,
        @PathVariable Integer commenterId,
        @RequestBody ReplyCreateRequest request) { // ReplyCreateRequest로 변경

        // 대댓글 생성
        Reply newReply = replyService.createReply(qnaId, commenterId,
            request.getContent()); // 요청에서 content를 가져옴

        // 댓글 작성자의 프로필 이미지 가져오기
        User commenter = userRepository.findById(commenterId)
            .orElseThrow(
                () -> new IllegalArgumentException("댓글 작성자를 찾을 수 없습니다. ID: " + commenterId));

        String imgProf = commenter.getProfile().getImgProf(); // 댓글 작성자의 프로필 이미지

        // ReplyResponse 객체 생성
        ReplyResponse replyResponse = new ReplyResponse(
            newReply.getReplyId(),
            commenterId,
            newReply.getContent(),
            qnaId,
            imgProf,
            newReply.getDeleted(), // 삭제 여부
            newReply.getDeleted() ? "삭제된 댓글입니다." : null,// 삭제 메시지
            newReply.getLikes()
        );

        // ReplyCreateResponse 반환
        return ResponseEntity.ok(ReplyCreateResponse.success("대댓글이 작성되었습니다.", replyResponse));
    }


    // 대댓글 좋아요 토글
    @PostMapping("/replies/{replyId}/likes")
    public ResponseEntity<Response> toggleLikeReply(@PathVariable Integer replyId,
        Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()); // 로그인된 사용자 정보 가져오기
        replyService.toggleLike(replyId, user); // User 객체를 전달하여 좋아요 상태 변경
        return ResponseEntity.ok(Response.successMessage("좋아요 상태가 변경되었습니다."));
    }


    // 대댓글 삭제
    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<Response> deleteReply(@PathVariable Integer replyId) {
        replyService.deleteReply(replyId); // 소프트 삭제 호출
        return ResponseEntity.ok(Response.successMessage("대댓글이 삭제되었습니다.")); // 성공 메시지 반환
    }

    // 대댓글 정보 조회
    @GetMapping("/replies/{replyId}")
    public ResponseEntity<ReplyResponse> getReply(@PathVariable Integer replyId) {
        Reply reply = replyService.getReply(replyId);

        return getReplyResponseEntity(reply);
    }

    private ResponseEntity<ReplyResponse> getReplyResponseEntity(Reply reply) {
        if (reply.getDeleted()) {
            return ResponseEntity.ok(new ReplyResponse(
                reply.getReplyId(),
                null,  // userId는 null
                "삭제된 댓글입니다.",
                reply.getQna().getQnaId(),
                null,  // imgProf는 null
                true,  // deleted 여부
                "삭제된 댓글입니다.", // 삭제 메시지
                0L // 좋아요 수는 0으로 설정
            ));
        }

        return ResponseEntity.ok(new ReplyResponse(
            reply.getReplyId(),
            reply.getCommenter().getUserId(),
            reply.getContent(),
            reply.getQna().getQnaId(),
            reply.getCommenter().getProfile().getImgProf(),
            false,  // deleted 여부
            null,   // 삭제 메시지는 필요 없음
            reply.getLikes() // 현재 좋아요 수
        ));
    }

}
