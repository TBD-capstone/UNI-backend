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
import uni.backend.service.PageTranslationService;
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
    private final PageTranslationService pageTranslationService;

    public QnaController(QnaService qnaService, UserRepository userRepository,
        UserService userService, PageTranslationService pageTranslationService) {
        this.qnaService = qnaService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.pageTranslationService = pageTranslationService;
    }

    // 특정 유저의 Qna 목록 조회
    @GetMapping("/user/{userId}/qnas")
    public ResponseEntity<List<QnaResponse>> getUserQnas(@PathVariable Integer userId,
        @RequestHeader(name = "Accept-Language", required = false) String acceptLanguage) {

        List<QnaResponse> response = qnaService.getUserQnas(userId);

        if (acceptLanguage != null && !acceptLanguage.isEmpty() && !acceptLanguage.equals("ko")) {
            pageTranslationService.translateQna(response, acceptLanguage);
        }

        return ResponseEntity.ok(response);
    }


    // 특정 유저의 Qna 작성
    @PostMapping("/user/{userId}/qnas/{commenterId}")
    public ResponseEntity<QnaCreateResponse> createQna(
        @PathVariable Integer userId,
        @PathVariable Integer commenterId,
        @RequestBody QnaCreateRequest request) {

        // 서비스 호출 및 결과 반환
        QnaResponse qnaResponse = qnaService.createQna(userId, commenterId, request.getContent());
        return ResponseEntity.ok(QnaCreateResponse.success("Qna가 성공적으로 작성되었습니다.", qnaResponse));
    }

    // Qna 좋아요
    /*@PostMapping("/qnas/{qnaId}/likes")
    public ResponseEntity<Response> toggleLike(@PathVariable Integer qnaId,
        Authentication authentication) {
        Optional<User> user = userService.findByEmail(authentication.getName()); // 로그인된 사용자 정보 가져오기
        Qna updatedQna = qnaService.toggleLike(qnaId, user.orElse(null)); // QnaService에서 좋아요 토글 처리
        return ResponseEntity.ok(Response.successMessage("좋아요 상태가 변경되었습니다."));
    }*/


    // Qna 삭제
    @DeleteMapping("/qnas/{qnaId}")
    public ResponseEntity<Response> deleteQna(@PathVariable Integer qnaId) {
        Qna deletedQna = qnaService.deleteQna(qnaId);
        return ResponseEntity.ok(Response.successMessage("Qna가 삭제되었습니다."));
    }
}
