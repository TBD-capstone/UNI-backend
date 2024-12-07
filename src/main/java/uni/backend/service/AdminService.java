package uni.backend.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Qna;
import uni.backend.domain.Reply;
import uni.backend.domain.Report;
import uni.backend.domain.Review;
import uni.backend.domain.ReviewReply;
import uni.backend.domain.User;
import uni.backend.domain.UserStatus;
import uni.backend.domain.dto.ReportedUserResponse;
import uni.backend.domain.dto.UserResponse;
import uni.backend.util.AdminAccountUtil;
import uni.backend.repository.ProfileRepository;
import uni.backend.repository.QnaRepository;
import uni.backend.repository.ReplyRepository;
import uni.backend.repository.ReportRepository;
import uni.backend.repository.ReviewReplyRepository;
import uni.backend.repository.ReviewRepository;
import uni.backend.repository.UserRepository;

import java.time.LocalDateTime;

/**
 * 관리자 서비스 관리자 계정 생성, 유저 관리 및 신고된 유저 리스트 조회 기능을 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final AdminAccountUtil adminAccountUtil;
    private final QnaRepository qnaRepository;
    private final ReviewRepository reviewRepository;
    private final ProfileRepository profileRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final ReplyRepository replyRepository;

    /**
     * 관리자 계정 생성 관리자 계정을 생성하고, 생성된 계정 정보를 로그로 기록, 및 팀원에게 이메일 전송
     */
    @Transactional
    public void createAccount(List<String> recipientEmails) {
        String rawPassword = adminAccountUtil.createAdminPassword();
        User admin = adminAccountUtil.createAdminAccount(rawPassword);
        userRepository.save(admin);

        recipientEmails.forEach(recipientEmail -> {
            SimpleMailMessage message = AdminAccountUtil.createEmailForm(
                recipientEmail, admin.getEmail(), rawPassword);
            message.setFrom("jdragon@uni-ajou.site");
            javaMailSender.send(message);
        });

        log.info("관리자 계정이 생성되었으며, 이메일이 발송되었습니다.");
    }

    /**
     * 모든 유저 리스트 조회 유저 상태와 페이지 정보를 기반으로 유저 리스트를 조회합니다.
     *
     * @param status 유저 상태 (ACTIVE, INACTIVE, BANNED)
     * @param page   조회할 페이지 번호
     * @param size   페이지 크기
     * @return 페이징 처리된 유저 리스트
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(UserStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("email").ascending());
        Page<User> users = (status != null)
            ? userRepository.findByStatus(status, pageable)
            : userRepository.findAll(pageable);

        log.info("유저 리스트 조회: 상태={}, 페이지={}", status, page);
        return users.map(user -> new UserResponse(
            user.getUserId(),
            user.getEmail(),
            user.getName(),
            user.getStatus() != null ? user.getStatus().name() : null,
            user.getUnivName(),
            user.getRole() != null ? user.getRole().name() : null,
            user.getLastReportReason(),
            user.getReportCount(),
            user.getEndBanDate()
        ));
    }

    /**
     * 유저 상태 변경 및 제재 날짜 설정 유저의 상태를 변경하고, 블라인드 처리를 수행합니다.
     *
     * @param userId  유저 ID
     * @param status  변경할 유저 상태
     * @param banDays 제재 기간 (일 단위)
     */
    @Transactional
    public void updateUserStatus(Integer userId, UserStatus status, Integer banDays) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime banEndDate = null;

        if (banDays != null) {
            if (banDays == 0) {
                status = UserStatus.ACTIVE;
                unblindAllContentByUser(userId);
            } else {
                banEndDate = LocalDateTime.now().plusDays(banDays);
                blindAllContentByUser(userId);
            }
        }

        user.setStatus(status);
        user.setEndBanDate(banEndDate);
        userRepository.save(user);

        UserStatus finalStatus = status;
        profileRepository.findByUser_UserId(userId)
            .ifPresent(profile -> profile.setVisible(finalStatus != UserStatus.BANNED));

        log.info("유저 ID={}의 상태가 {}로 변경되었습니다. 제재 해제일: {}", userId, finalStatus, banEndDate);
    }

    /**
     * 신고된 유저 리스트 조회 신고 데이터를 기반으로 신고된 유저 리스트를 페이징 처리하여 반환합니다.
     *
     * @param page 조회할 페이지 번호
     * @param size 페이지 크기
     * @return 페이징 처리된 신고된 유저 리스트
     */
    @Transactional(readOnly = true)
    public Page<ReportedUserResponse> getReportedUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Report> allReports = reportRepository.findAll();

        Map<Integer, List<Report>> groupedReports = allReports.stream()
            .collect(Collectors.groupingBy(report -> report.getReportedUser().getUserId()));

        List<ReportedUserResponse> reportedUserResponses = groupedReports.entrySet().stream()
            .map(entry -> {
                Integer userId = entry.getKey();
                List<Report> reports = entry.getValue();

                return new ReportedUserResponse(
                    userId,
                    reports.get(0).getReportedUser().getEmail(),
                    (long) reports.size(),
                    reports.stream()
                        .map(report -> new ReportedUserResponse.ReportDetail(
                            report.getTitle(),
                            report.getCategory().name(),
                            report.getReason().name(),
                            report.getDetailedReason()
                        ))
                        .collect(Collectors.toList())
                );
            })
            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), reportedUserResponses.size());
        List<ReportedUserResponse> pagedReports = reportedUserResponses.subList(start, end);

        return new PageImpl<>(pagedReports, pageable, reportedUserResponses.size());
    }

    /**
     * 유저의 모든 콘텐츠 블라인드 처리 특정 유저의 QnA, 리뷰, 답글 등을 블라인드 처리합니다.
     *
     * @param userId 블라인드 처리할 유저 ID
     */
    @Transactional
    public void blindAllContentByUser(Integer userId) {
        // QnA 블라인드 처리
        List<Qna> qnas = qnaRepository.findByCommenter_UserId(userId);
        qnas.forEach(Qna::blindQna);
        qnaRepository.saveAll(qnas);
        log.info("유저 ID={}의 모든 QnA를 블라인드 처리했습니다.", userId);

        // QnA 답글 블라인드 처리
        List<Reply> replies = replyRepository.findByCommenter_UserId(userId);
        replies.forEach(reply -> reply.setIsBlind(true));
        replyRepository.saveAll(replies);
        log.info("유저 ID={}의 모든 QnA 답글을 블라인드 처리했습니다.", userId);

        // 리뷰 블라인드 처리
        List<Review> reviews = reviewRepository.findByCommenter_UserId(userId);
        reviews.forEach(Review::blindReview);
        reviewRepository.saveAll(reviews);
        log.info("유저 ID={}의 모든 리뷰를 블라인드 처리했습니다.", userId);

        // 리뷰 리플라이 블라인드 처리
        List<ReviewReply> reviewReplies = reviewReplyRepository.findByCommenter_UserId(userId);
        reviewReplies.forEach(reply -> reply.setIsBlind(true));
        reviewReplyRepository.saveAll(reviewReplies);
        log.info("유저 ID={}의 모든 리뷰 리플라이를 블라인드 처리했습니다.", userId);
    }

    /**
     * 유저의 모든 콘텐츠 블라인드 해제 특정 유저의 QnA, 리뷰, 답글 등을 블라인드 해제합니다.
     *
     * @param userId 블라인드 해제할 유저 ID
     */
    @Transactional
    public void unblindAllContentByUser(Integer userId) {
        // QnA 블라인드 해제
        List<Qna> qnas = qnaRepository.findByCommenter_UserId(userId);
        qnas.forEach(Qna::unblindQna);
        qnaRepository.saveAll(qnas);
        log.info("유저 ID={}의 모든 QnA 블라인드 상태를 해제했습니다.", userId);

        // QnA 답글 블라인드 해제
        List<Reply> replies = replyRepository.findByCommenter_UserId(userId);
        replies.forEach(reply -> reply.setIsBlind(false));
        replyRepository.saveAll(replies);
        log.info("유저 ID={}의 모든 QnA 답글 블라인드 상태를 해제했습니다.", userId);

        // 리뷰 블라인드 해제
        List<Review> reviews = reviewRepository.findByCommenter_UserId(userId);
        reviews.forEach(Review::unblindReview);
        reviewRepository.saveAll(reviews);
        log.info("유저 ID={}의 블라인드 리뷰 상태를 해제했습니다.", userId);

        // 리뷰 리플라이 블라인드 해제
        List<ReviewReply> reviewReplies = reviewReplyRepository.findByCommenter_UserId(userId);
        reviewReplies.forEach(reply -> reply.setIsBlind(false));
        reviewReplyRepository.saveAll(reviewReplies);
        log.info("유저 ID={}의 모든 리뷰 리플라이 블라인드 상태를 해제했습니다.", userId);
    }
}
