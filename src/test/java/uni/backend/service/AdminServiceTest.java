package uni.backend.service;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uni.backend.domain.*;
import uni.backend.util.AdminAccountUtil;
import uni.backend.repository.*;

class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;
    @Mock
    private ReportCategory reportCategory;
    @Mock
    private ReportReason reportReason;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private AdminAccountUtil adminAccountUtil;

    @Mock
    private QnaRepository qnaRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ReviewReplyRepository reviewReplyRepository;

    @Mock
    private ReplyRepository replyRepository;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder()
            .userId(1)
            .email("test@example.com")
            .name("Test User")
            .status(UserStatus.ACTIVE)
            .build();
    }

//    @Test
//    @DisplayName("관리자 계정 생성 성공 테스트")
//    void createAccount_성공() {
//        // given
//        String rawPassword = "randomPassword123";
//        User admin = User.builder()
//            .userId(99)
//            .email("admin@example.com")
//            .password(rawPassword)
//            .build();
//
//        when(adminAccountUtil.createAdminPassword()).thenReturn(rawPassword);
//        when(adminAccountUtil.createAdminAccount(rawPassword)).thenReturn(admin);
//        when(userRepository.save(any(User.class))).thenReturn(admin);
//
//        // when
//        adminService.createAccount();
//
//        // then
//        verify(adminAccountUtil).createAdminPassword();
//        verify(adminAccountUtil).createAdminAccount(rawPassword);
//        verify(userRepository).save(admin);
//    }

//    @Test
//    @DisplayName("유저 상태 업데이트 성공 테스트")
//    void updateUserStatus_성공() {
//        // given
//        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
//        doNothing().when(qnaRepository).setBlindStatusByUserId(user.getUserId(), true);
//        // when
//        adminService.updateUserStatus(user.getUserId(), UserStatus.BANNED, 7);
//
//        // then
//        assertEquals(UserStatus.BANNED, user.getStatus());
//        assertNotNull(user.getEndBanDate());
//        verify(userRepository).save(user);
//        verify(qnaRepository).setBlindStatusByUserId(user.getUserId(), true);
//    }
//
//    @Test
//    @DisplayName("신고된 유저 리스트 조회 성공 테스트")
//    void getReportedUsers_성공() {
//        // given
//        User reportedUser = User.builder()
//            .userId(2)
//            .email("reported@example.com")
//            .build();
//
//        Report report1 = Report.builder()
//            .reportedUser(reportedUser)
//            .category(ReportCategory.CHAT)
//            .reason(ReportReason.SPAM)
//            .detailedReason("Spam content")
//            .build();
//
//        List<Report> reports = List.of(report1);
//
//        when(reportRepository.findAll()).thenReturn(reports);
//
//        // when
//        var result = adminService.getReportedUsers(1, 10);
//
//        // then
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//        verify(reportRepository).findAll();
//    }

    @Test
    @DisplayName("유저 콘텐츠 블라인드 처리 성공 테스트")
    void blindAllContentByUser_성공() {
        // given
        List<Qna> qnas = new ArrayList<>();
        List<Reply> replies = new ArrayList<>();
        List<Review> reviews = new ArrayList<>();
        List<ReviewReply> reviewReplies = new ArrayList<>();

        Qna qna = new Qna();
        qnas.add(qna);

        when(qnaRepository.findByCommenter_UserId(user.getUserId())).thenReturn(qnas);
        when(replyRepository.findByCommenter_UserId(user.getUserId())).thenReturn(replies);
        when(reviewRepository.findByCommenter_UserId(user.getUserId())).thenReturn(reviews);
        when(reviewReplyRepository.findByCommenter_UserId(user.getUserId())).thenReturn(
            reviewReplies);

        // when
        adminService.blindAllContentByUser(user.getUserId());

        // then
        verify(qnaRepository).findByCommenter_UserId(user.getUserId());
        verify(replyRepository).findByCommenter_UserId(user.getUserId());
        verify(reviewRepository).findByCommenter_UserId(user.getUserId());
        verify(reviewReplyRepository).findByCommenter_UserId(user.getUserId());
    }

    @Test
    @DisplayName("유저 콘텐츠 블라인드 해제 성공 테스트")
    void unblindAllContentByUser_성공() {
        // given
        List<Qna> qnas = new ArrayList<>();
        List<Reply> replies = new ArrayList<>();
        List<Review> reviews = new ArrayList<>();
        List<ReviewReply> reviewReplies = new ArrayList<>();

        Qna qna = new Qna();
        qnas.add(qna);

        when(qnaRepository.findByCommenter_UserId(user.getUserId())).thenReturn(qnas);
        when(replyRepository.findByCommenter_UserId(user.getUserId())).thenReturn(replies);
        when(reviewRepository.findByCommenter_UserId(user.getUserId())).thenReturn(reviews);
        when(reviewReplyRepository.findByCommenter_UserId(user.getUserId())).thenReturn(
            reviewReplies);

        // when
        adminService.unblindAllContentByUser(user.getUserId());

        // then
        verify(qnaRepository).findByCommenter_UserId(user.getUserId());
        verify(replyRepository).findByCommenter_UserId(user.getUserId());
        verify(reviewRepository).findByCommenter_UserId(user.getUserId());
        verify(reviewReplyRepository).findByCommenter_UserId(user.getUserId());
    }
}
