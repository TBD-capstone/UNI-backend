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
import uni.backend.domain.dto.ReportedUserResponse.ReportDetail;
import uni.backend.domain.util.AdminAccountUtil;
import uni.backend.repository.ProfileRepository;
import uni.backend.repository.QnaRepository;
import uni.backend.repository.ReplyRepository;
import uni.backend.repository.ReportRepository;
import uni.backend.repository.ReviewReplyRepository;
import uni.backend.repository.ReviewRepository;
import uni.backend.repository.UserRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final AdminAccountUtil adminAccountUtil;
    private final QnaRepository qnaRepository;
    private final ReviewRepository reviewRepository;
    private final ProfileRepository profileRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final ReplyRepository replyRepository;

    /**
     * 관리자 계정 생성
     */
    @Transactional
    public void createAccount() {
        String rawPassword = adminAccountUtil.createAdminPassword();
        User admin = adminAccountUtil.createAdminAccount(rawPassword);
        userRepository.save(admin);
        log.info("관리자 계정이 생성되었습니다.");

    }

    /**
     * 모든 유저 리스트 조회
     *
     * @param status   유저 상태 (ACTIVE, INACTIVE, BANNED)
     * @param pageable 페이징 정보
     * @return 페이징 처리된 유저 리스트
     */
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(UserStatus status, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
            Sort.by("email").ascending());
        Page<User> result;

        if (status != null) {
            result = userRepository.findByStatus(status, pageRequest);
            log.info("유저 리스트 조회: 상태={}, 페이지={}", status, pageable.getPageNumber());
        } else {
            result = userRepository.findAll(pageRequest);
            log.info("전체 유저 리스트 조회: 페이지={}", pageable.getPageNumber());
        }

        return result;
    }

    /**
     * 유저 상태 및 제재 날짜 설정
     *
     * @param userId     유저 ID
     * @param status     변경할 유저 상태
     * @param banEndDate 제재 해제일
     */
    @Transactional
    public void updateUserStatus(Integer userId, UserStatus status, LocalDateTime banEndDate) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (banEndDate != null && banEndDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("제재 종료날짜는 과거로 설정할 수 업습니다..");
        }

        user.setStatus(status);
        user.setEndBanDate(banEndDate);
        userRepository.save(user);

        qnaRepository.setBlindStatusByUserId(userId, status == UserStatus.BANNED);
        if (status == UserStatus.BANNED) {
            blindAllContentByUser(userId); //  블라인드 처리 통합 메서드
        } else if (status == UserStatus.ACTIVE) {
            unblindAllContentByUser(userId); // 블라인드 해제 통합 메서드
        }

        // 프로필 노출 설정
        profileRepository.findByUser_UserId(userId)
            .ifPresent(profile -> profile.setVisible(status != UserStatus.BANNED));

        log.info("유저 ID={}의 상태가 {}로 변경되었습니다. 제재 해제일: {}", userId, status, banEndDate);
    }

    /**
     * 신고된 유저 리스트 조회 (페이징 처리)
     *
     * @param pageable 페이징 정보
     * @return 페이징 처리된 신고된 유저 리스트
     */
    @Transactional(readOnly = true)
    public Page<ReportedUserResponse> getReportedUsers(Pageable pageable) {
        // 모든 신고된 유저의 데이터를 가져옵니다.
        List<Report> allReports = reportRepository.findAll();

        // 유저별로 신고를 그룹화하고, ReportedUserResponse 형태로 변환합니다.
        Map<Integer, List<Report>> groupedReports = allReports.stream()
            .collect(Collectors.groupingBy(report -> report.getReportedUser().getUserId()));

        List<ReportedUserResponse> reportedUserResponses = groupedReports.entrySet().stream()
            .map(entry -> {
                Integer userId = entry.getKey();
                List<Report> reports = entry.getValue();

                // 신고당한 유저의 정보와 신고된 내역들을 DTO로 변환합니다.
                return new ReportedUserResponse(
                    userId,
                    reports.get(0).getReportedUser().getEmail(),
                    (long) reports.size(),
                    reports.stream().map(report -> new ReportedUserResponse.ReportDetail(
                        report.getCategory().name(),
                        report.getReason().name(),
                        report.getDetailedReason()
                    )).collect(Collectors.toList())
                );
            })
            .collect(Collectors.toList());

        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), reportedUserResponses.size());
        List<ReportedUserResponse> pagedReports = reportedUserResponses.subList(start, end);

        return new PageImpl<>(pagedReports, pageable, reportedUserResponses.size());
    }

    /**
     * 유저의 모든 콘텐츠 블라인드 처리
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
