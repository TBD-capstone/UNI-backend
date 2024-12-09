package uni.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import uni.backend.domain.*;
import uni.backend.domain.dto.ReportedUserResponse;
import uni.backend.domain.dto.UserResponse;
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
        String rawPassword = "randomPassword123";
        User admin = User.builder()
            .userId(99)
            .email("admin@example.com")
            .password(rawPassword)
            .build();

        when(adminAccountUtil.createAdminPassword()).thenReturn(rawPassword);
        when(adminAccountUtil.createAdminAccount(rawPassword)).thenReturn(admin);
        when(userRepository.save(any(User.class))).thenReturn(admin);

        adminService.createAccount(List.of("recipient@example.com"));

        verify(adminAccountUtil).createAdminPassword();
        verify(adminAccountUtil).createAdminAccount(rawPassword);
        verify(userRepository).save(admin);
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("유저 상태 업데이트 성공 테스트")
    void updateUserStatus_성공() {
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

        adminService.updateUserStatus(user.getUserId(), UserStatus.BANNED, 7);

        assertEquals(UserStatus.BANNED, user.getStatus());
        assertNotNull(user.getEndBanDate());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("신고된 유저 리스트 조회 성공 테스트")
    void getReportedUsers_성공() {
        User reportedUser = User.builder()
            .userId(2)
            .email("reported@example.com")
            .build();

        Report report = Report.builder()
            .reportedUser(reportedUser)
            .category(ReportCategory.CHAT)
            .reason(ReportReason.SPAM)
            .detailedReason("Spam content")
            .title("Spam Report")
            .build();

        when(reportRepository.findAll()).thenReturn(List.of(report));

        Page<ReportedUserResponse> result = adminService.getReportedUsers(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reportRepository).findAll();
    }

    @Test
    @DisplayName("유저 콘텐츠 블라인드 처리 성공 테스트")
    void blindAllContentByUser_성공() {
        List<Qna> qnas = List.of(new Qna());
        List<Reply> replies = List.of(new Reply());
        List<Review> reviews = List.of(new Review());
        List<ReviewReply> reviewReplies = List.of(new ReviewReply());

        when(qnaRepository.findByCommenter_UserId(user.getUserId())).thenReturn(qnas);
        when(replyRepository.findByCommenter_UserId(user.getUserId())).thenReturn(replies);
        when(reviewRepository.findByCommenter_UserId(user.getUserId())).thenReturn(reviews);
        when(reviewReplyRepository.findByCommenter_UserId(user.getUserId())).thenReturn(
            reviewReplies);

        adminService.blindAllContentByUser(user.getUserId());

        verify(qnaRepository).findByCommenter_UserId(user.getUserId());
        verify(replyRepository).findByCommenter_UserId(user.getUserId());
        verify(reviewRepository).findByCommenter_UserId(user.getUserId());
        verify(reviewReplyRepository).findByCommenter_UserId(user.getUserId());
    }


    @Test
    @DisplayName("유저 상태 업데이트 - banDays null 처리")
    void updateUserStatus_banDaysNull() {
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

        adminService.updateUserStatus(user.getUserId(), UserStatus.ACTIVE, null);

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("신고된 유저 리스트 조회 - 신고 없음")
    void getReportedUsers_noReports() {
        when(reportRepository.findAll()).thenReturn(List.of());

        Page<ReportedUserResponse> result = adminService.getReportedUsers(0, 10);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements()); // 신고가 없으므로 0개
    }

    @Test
    @DisplayName("유저 콘텐츠 블라인드 처리 - 모든 콘텐츠 이미 블라인드")
    void blindAllContentByUser_alreadyBlind() {
        Qna qna = new Qna();
        qna.blindQna(); // 이미 블라인드 처리됨

        when(qnaRepository.findByCommenter_UserId(user.getUserId())).thenReturn(List.of(qna));
        adminService.blindAllContentByUser(user.getUserId());

        verify(qnaRepository).findByCommenter_UserId(user.getUserId());
        verify(qnaRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("관리자 계정 생성 - 잘못된 이메일 예외 테스트")
    void createAccount_invalidEmail() {
        String rawPassword = "randomPassword123";
        User admin = User.builder()
            .userId(99)
            .email("admin@example.com")
            .password(rawPassword)
            .build();

        when(adminAccountUtil.createAdminPassword()).thenReturn(rawPassword);
        when(adminAccountUtil.createAdminAccount(rawPassword)).thenReturn(admin);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> adminService.createAccount(List.of(""))); // 빈 문자열 이메일
        assertEquals("잘못된 이메일 주소입니다.", exception.getMessage());
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("유저 콘텐츠 블라인드 해제 성공")
    void unblindAllContentByUser_성공() {
        // given
        List<Qna> qnas = List.of(new Qna());
        List<Reply> replies = List.of(new Reply());
        List<Review> reviews = List.of(new Review());
        List<ReviewReply> reviewReplies = List.of(new ReviewReply());

        when(userRepository.existsById(user.getUserId())).thenReturn(true);
        when(qnaRepository.findByCommenter_UserId(user.getUserId())).thenReturn(qnas);
        when(replyRepository.findByCommenter_UserId(user.getUserId())).thenReturn(replies);
        when(reviewRepository.findByCommenter_UserId(user.getUserId())).thenReturn(reviews);
        when(reviewReplyRepository.findByCommenter_UserId(user.getUserId())).thenReturn(
            reviewReplies);

        // when
        adminService.unblindAllContentByUser(user.getUserId());

        // then
        verify(qnaRepository).saveAll(qnas);
        verify(replyRepository).saveAll(replies);
        verify(reviewRepository).saveAll(reviews);
        verify(reviewReplyRepository).saveAll(reviewReplies);
    }


    @Test
    @DisplayName("유저 콘텐츠 블라인드 해제 - 유저 없음")
    void unblindAllContentByUser_유저없음() {
        // given
        when(userRepository.existsById(user.getUserId())).thenReturn(false);

        // when & then
        assertThrows(IllegalArgumentException.class,
            () -> adminService.unblindAllContentByUser(user.getUserId()));
        verify(userRepository).existsById(user.getUserId());
    }


    @Test
    @DisplayName("유저 리스트 조회 - 모든 유저")
    void getAllUsers_모든유저() {
        // given
        Page<User> users = new PageImpl<>(List.of(user)); // Mock Page 데이터 생성
        when(userRepository.findAll(any(Pageable.class))).thenReturn(users);

        // when
        Page<UserResponse> result = adminService.getAllUsers(null, 0, 10);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(user.getEmail(), result.getContent().get(0).getEmail());
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("유저 리스트 조회 - 특정 상태 ACTIVE")
    void getAllUsers_statusFilter_ACTIVE() {
        // given
        Page<User> users = new PageImpl<>(List.of(user)); // Mock Page 데이터 생성
        when(userRepository.findByStatus(eq(UserStatus.ACTIVE), any(Pageable.class))).thenReturn(
            users);

        // when
        Page<UserResponse> result = adminService.getAllUsers(UserStatus.ACTIVE, 0, 10);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(user.getEmail(), result.getContent().get(0).getEmail());
        verify(userRepository).findByStatus(eq(UserStatus.ACTIVE), any(Pageable.class));
    }

    @Test
    @DisplayName("유저 리스트 조회 - 특정 상태 BANNED")
    void getAllUsers_statusFilter_BANNED() {
        // given
        Page<User> users = new PageImpl<>(List.of(user)); // Mock Page 데이터 생성
        when(userRepository.findByStatus(eq(UserStatus.BANNED), any(Pageable.class))).thenReturn(
            users);

        // when
        Page<UserResponse> result = adminService.getAllUsers(UserStatus.BANNED, 0, 10);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(user.getEmail(), result.getContent().get(0).getEmail());
        verify(userRepository).findByStatus(eq(UserStatus.BANNED), any(Pageable.class));
    }

    @Test
    @DisplayName("유저 리스트 조회 - 특정 상태 INACTIVE")
    void getAllUsers_statusFilter_INACTIVE() {
        // given
        Page<User> users = new PageImpl<>(List.of(user)); // Mock Page 데이터 생성
        when(userRepository.findByStatus(eq(UserStatus.INACTIVE), any(Pageable.class))).thenReturn(
            users);

        // when
        Page<UserResponse> result = adminService.getAllUsers(UserStatus.INACTIVE, 0, 10);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(user.getEmail(), result.getContent().get(0).getEmail());
        verify(userRepository).findByStatus(eq(UserStatus.INACTIVE), any(Pageable.class));
    }


}
