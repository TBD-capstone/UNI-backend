package uni.backend.controller;

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
        adminService.createAccount();
        return ResponseEntity.ok("관리자 계정이 생성되었습니다.");
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
        Pageable pageable) {

        Page<User> users = adminService.getAllUsers(status, pageable);
        Page<UserResponse> userDtos = users.map(user -> {
            return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getStatus() != null ? user.getStatus().name() : null, // NPE 방지
                user.getUnivName(),
                user.getRole() != null ? user.getRole().name() : null, // NPE 방지
                user.getLastReportReason(),
                user.getReportCount(),
                user.getEndBanDate()
            );
        });

        return ResponseEntity.ok(userDtos);
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
        @RequestParam(required = false) Integer banDays
    ) {
        LocalDateTime banEndDate = null;
        if (banDays != null) {
            banEndDate = LocalDateTime.now().plusDays(banDays);  // plusDays() 메서드 사용으로 변경
        }
        adminService.updateUserStatus(userId, status, banEndDate);
        return ResponseEntity.ok("유저 상태가 성공적으로 업데이트되었습니다.");
    }

    /**
     * 신고된 유저 목록 조회
     *
     * @param pageable 페이징 정보
     * @return 신고된 유저 리스트 (신고 사유 포함)
     */
    @GetMapping("/reported-users")
    public ResponseEntity<Page<ReportedUserResponse>> getReportedUsers(Pageable pageable) {
        Page<ReportedUserResponse> reportedUsers = adminService.getReportedUsers(pageable);
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