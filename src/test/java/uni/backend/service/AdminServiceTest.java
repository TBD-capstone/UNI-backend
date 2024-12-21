package uni.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mail.SimpleMailMessage;

import org.springframework.mail.javamail.JavaMailSender;
import uni.backend.domain.*;
import uni.backend.domain.dto.ReportedUserResponse;
import uni.backend.repository.*;
import uni.backend.util.AdminAccountUtil;

class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private AdminAccountUtil adminAccountUtil;
    @Mock
    private QnaRepository qnaRepository;
    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ReviewReplyRepository reviewReplyRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private SimpleMailMessage simpleMailMessage;
    @Mock
    private JavaMailSender javaMailSender;


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

    @Test
    @DisplayName("관리자 계정 생성 성공 테스트")
    void createAccount_성공() {
        // given
        String rawPassword = "randomPassword123";
        User admin = User.builder()
            .userId(99)
            .email("admin@example.com")
            .password(rawPassword)
            .build();

        when(adminAccountUtil.createAdminPassword()).thenReturn(rawPassword);
        when(adminAccountUtil.createAdminAccount(rawPassword)).thenReturn(admin);
        when(userRepository.save(any(User.class))).thenReturn(admin);

        // when
        adminService.createAccount(List.of("recipient@example.com"));

        // then
        verify(adminAccountUtil).createAdminPassword();
        verify(adminAccountUtil).createAdminAccount(rawPassword);
        verify(userRepository).save(admin);
    }

    @Test
    @DisplayName("관리자 계정 생성 - 이메일 전송 실패 테스트")
    void createAccount_이메일실패() {
        // given
        String rawPassword = "randomPassword123";
        User admin = User.builder()
            .userId(99)
            .email("admin@example.com")
            .password(rawPassword)
            .build();

        when(adminAccountUtil.createAdminPassword()).thenReturn(rawPassword);
        when(adminAccountUtil.createAdminAccount(rawPassword)).thenReturn(admin);
        when(userRepository.save(any(User.class))).thenReturn(admin);

        doThrow(RuntimeException.class).when(javaMailSender).send(any(SimpleMailMessage.class));

        // when & then
        assertThrows(RuntimeException.class,
            () -> adminService.createAccount(List.of("recipient@example.com")));
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("유저 상태 업데이트 성공 테스트")
    void updateUserStatus_성공() {
        // given
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

        // when
        adminService.updateUserStatus(user.getUserId(), UserStatus.BANNED, 7);

        // then
        assertEquals(UserStatus.BANNED, user.getStatus());
        assertNotNull(user.getEndBanDate());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("유저 상태 업데이트 - 유저 없음 예외 테스트")
    void updateUserStatus_유저없음_예외() {
        // given
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class,
            () -> adminService.updateUserStatus(1, UserStatus.BANNED, 7));
        verify(userRepository).findById(anyInt());
    }

    @Test
    @DisplayName("신고된 유저 리스트 조회 성공 테스트")
    void getReportedUsers_성공() {
        // given
        User reporterUser = User.builder()
            .userId(1)
            .name("Reporter Name") // getName() 호출을 위해 필요한 값
            .email("reporter@example.com")
            .build();

        User reportedUser = User.builder()
            .userId(2)
            .email("reported@example.com")
            .build();

        Report report = Report.builder()
            .reporterUser(reporterUser) // reporterUser 설정
            .reportedUser(reportedUser)
            .category(ReportCategory.CHAT)
            .reason(ReportReason.SPAM)
            .detailedReason("Spam content")
            .title("Spam Report")
            .build();

        when(reportRepository.findAll()).thenReturn(List.of(report));

        // when
        Page<ReportedUserResponse> result = adminService.getReportedUsers(0, 10);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reportRepository).findAll();
    }

    @Test
    @DisplayName("유저 콘텐츠 블라인드 처리 성공 테스트")
    void blindAllContentByUser_성공() {
        // given
        List<Qna> qnas = List.of(new Qna());
        List<Reply> replies = List.of(new Reply());
        List<Review> reviews = List.of(new Review());
        List<ReviewReply> reviewReplies = List.of(new ReviewReply());

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
        List<Qna> qnas = List.of(new Qna());
        List<Reply> replies = List.of(new Reply());
        List<Review> reviews = List.of(new Review());
        List<ReviewReply> reviewReplies = List.of(new ReviewReply());

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
