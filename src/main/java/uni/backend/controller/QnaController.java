package uni.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.Qna;
import uni.backend.domain.User;
import uni.backend.domain.dto.QnaCreateRequest;
import uni.backend.domain.dto.QnaCreateResponse;
import uni.backend.domain.dto.QnaResponse;
import uni.backend.domain.dto.QnaUserResponse;
import uni.backend.domain.dto.ReplyResponse;
import uni.backend.domain.dto.Response;
import uni.backend.repository.UserRepository;
import uni.backend.service.QnaService;

import java.util.List;
import java.util.Optional;

import uni.backend.service.UserService;

@RestController
@RequestMapping("/api")
public class QnaController {

    private final QnaService qnaService;
    private final UserRepository userRepository;
    private final UserService userService;

    public QnaController(QnaService qnaService, UserRepository userRepository,
        UserService userService) {
        this.qnaService = qnaService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    // 특정 유저의 Qna 목록 조회
    @GetMapping("/user/{userId}/qnas")
    public ResponseEntity<List<QnaResponse>> getUserQnas(@PathVariable Integer userId) {
        List<Qna> userQnas = qnaService.getQnasByUserId(userId);

        List<QnaResponse> qnaResponses = userQnas.stream().map(qna -> {
            User profileOwner = qna.getProfileOwner();
            User commentAuthor = qna.getCommenter();

            QnaUserResponse ownerResponse = new QnaUserResponse(profileOwner.getUserId(),
                profileOwner.getName());
            QnaUserResponse commentAuthorResponse = new QnaUserResponse(commentAuthor.getUserId(),
                commentAuthor.getName());

            // 대댓글 리스트를 ReplyResponse 리스트로 변환
            List<ReplyResponse> replyResponses = qna.getReplies().stream().map(reply -> {
                return new ReplyResponse(
                    reply.getReplyId(),
                    reply.getCommenter().getUserId(),
                    reply.getCommenter().getName(),
                    reply.getContent(),
                    reply.getQna().getQnaId(),
                    reply.getCommenter().getProfile().getImgProf(),
                    reply.getDeleted(),
                    reply.getDeleted() ? "삭제된 대댓글입니다." : null,
                    reply.getLikes()
                );
            }).toList();

            return new QnaResponse(
                qna.getQnaId(),
                ownerResponse,
                commentAuthorResponse,
                qna.getBlindQna(),
                replyResponses, // 변환된 대댓글 리스트 추가
                profileOwner.getProfile().getImgProf(), // 프로필 이미지
                qna.getDeleted(), // 삭제 여부
                qna.getDeleted() ? "삭제된 Qna입니다." : null, // 삭제된 경우 메시지
                qna.getLikes()
            );
        }).toList();

        return ResponseEntity.ok(qnaResponses);
    }


    // 특정 유저의 Qna 작성
    @PostMapping("/user/{userId}/qnas/{commenterId}")
    public ResponseEntity<QnaCreateResponse> createQna(
        @PathVariable Integer userId,
        @PathVariable Integer commenterId,
        @RequestBody QnaCreateRequest request) {

        Qna newQna = qnaService.createQna(userId, commenterId, request.getContent());

        User profileOwner = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("프로필 주인을 찾을 수 없습니다. ID: " + userId));
        QnaUserResponse ownerResponse = new QnaUserResponse(profileOwner.getUserId(),
            profileOwner.getName());

        User commenter = userRepository.findById(commenterId)
            .orElseThrow(
                () -> new IllegalArgumentException("댓글 작성자를 찾을 수 없습니다. ID: " + commenterId));
        QnaUserResponse commentAuthorResponse = new QnaUserResponse(commenter.getUserId(),
            commenter.getName());

        QnaResponse qnaResponse = new QnaResponse(
            newQna.getQnaId(),
            ownerResponse,
            commentAuthorResponse,
            newQna.getContent(),
            null,
            profileOwner.getProfile().getImgProf(),
            newQna.getDeleted(),
            newQna.getDeleted() ? "삭제된 Qna입니다." : null,
            newQna.getLikes()
        );

        return ResponseEntity.ok(QnaCreateResponse.success("Qna가 성공적으로 작성되었습니다.", qnaResponse));
    }


    // Qna 좋아요
    @PostMapping("/qnas/{qnaId}/likes")
    public ResponseEntity<Response> toggleLike(@PathVariable Integer qnaId,
        Authentication authentication) {
        Optional<User> user = userService.findByEmail(authentication.getName()); // 로그인된 사용자 정보 가져오기
        Qna updatedQna = qnaService.toggleLike(qnaId, user.orElse(null)); // QnaService에서 좋아요 토글 처리
        return ResponseEntity.ok(Response.successMessage("좋아요 상태가 변경되었습니다."));
    }


    // Qna 삭제
    @DeleteMapping("/qnas/{qnaId}")
    public ResponseEntity<Response> deleteQna(@PathVariable Integer qnaId) {
        Qna deletedQna = qnaService.deleteQna(qnaId);
        return ResponseEntity.ok(Response.successMessage("Qna가 삭제되었습니다."));
    }
}
