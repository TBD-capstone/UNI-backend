package uni.backend.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uni.backend.domain.User;
import uni.backend.domain.UserStatus;
import uni.backend.domain.dto.ReportedUserResponse;
import uni.backend.domain.dto.UserResponse;
import uni.backend.service.AdminService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 관리자 계정 생성
     */
    @PostMapping("/create-account")
    public ResponseEntity<String> createAdminAccount() {
        List<String> recipientEmails = List.of(
            "gbe0808@ajou.ac.kr",
            "uko802@ajou.ac.kr",
            "ljy9085@ajou.ac.kr",
            "han1267@ajou.ac.kr",
            "nicola1928@ajou.ac.kr"
        );

        // recipientEmails 전달
        adminService.createAccount(recipientEmails);
        return ResponseEntity.ok("관리자 계정이 생성되었으며 이메일이 발송되었습니다.");
    }


    /**
     * 모든 유저 리스트 조회
     *
     * @param status   유저 상태 (ACTIVE, INACTIVE, BANNED)
     * @param pageable 페이징 정보
     * @return 페이징 처리된 유저 리스트
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
        @RequestParam(required = false) UserStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        Page<UserResponse> users = adminService.getAllUsers(status, page, size);
        return ResponseEntity.ok(users);
    }

    /**
     * 유저 상태 변경 및 제재 날짜 설정
     *
     * @param userId  유저 ID
     * @param status  변경할 유저 상태 (ACTIVE, BANNED)
     * @param banDays 제재 기간 (일 단위, Optional)
     * @return 변경 결과 메시지
     */
    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<String> updateUserStatus(
        @PathVariable Integer userId,
        @RequestParam UserStatus status,
        @RequestParam(required = false) Integer banDays) {

        adminService.updateUserStatus(userId, status, banDays);
        return ResponseEntity.ok("유저 상태가 성공적으로 업데이트되었습니다.");
    }

    /**
     * 신고된 유저 목록 조회
     *
     * @param pageable 페이징 정보
     * @return 신고된 유저 리스트 (신고 사유 포함)
     */
    @GetMapping("/reported-users")
    public ResponseEntity<Page<ReportedUserResponse>> getReportedUsers(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {

        Page<ReportedUserResponse> reportedUsers = adminService.getReportedUsers(page, size);
        return ResponseEntity.ok(reportedUsers);
    }


    /**
     * 유저의 모든 콘텐츠 블라인드 처리
     *
     * @param userId 유저 ID
     * @return 처리 결과 메시지
     */
    @PostMapping("/users/{userId}/blind-content")
    public ResponseEntity<String> blindAllUserContent(@PathVariable Integer userId) {
        adminService.blindAllContentByUser(userId);
        return ResponseEntity.ok("유저의 모든 콘텐츠가 블라인드 처리되었습니다.");
    }

    /**
     * 유저의 모든 콘텐츠 블라인드 해제
     *
     * @param userId 유저 ID
     * @return 처리 결과 메시지
     */
    @PostMapping("/users/{userId}/unblind-content")
    public ResponseEntity<String> unblindAllUserContent(@PathVariable Integer userId) {
        adminService.unblindAllContentByUser(userId);
        return ResponseEntity.ok("유저의 모든 콘텐츠 블라인드 상태가 해제되었습니다.");
    }
}
