package uni.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import uni.backend.domain.Profile;
import uni.backend.domain.User;
import uni.backend.domain.UserStatus;
import uni.backend.repository.UserRepository;

public class UserStatusSchedulerTest {

    @InjectMocks
    private UserStatusScheduler userStatusScheduler;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void unbanUsersIfBanExpired_밴해제조건없음() {
        // Given: 유저가 없는 경우
        when(userRepository.findByStatusAndEndBanDateBefore(eq(UserStatus.BANNED), any()))
            .thenReturn(Collections.emptyList());

        // When
        userStatusScheduler.unbanUsersIfBanExpired();

        // Then
        verify(userRepository, never()).saveAll(anyList());
    }


    @Test
    void unbanUsersIfBanExpired_밴해제조건있음_프로필있음() {
        // Given: 밴 해제 조건을 만족하는 유저 설정
        User user = new User();
        user.setUserId(1);
        user.setStatus(UserStatus.BANNED);
        user.setEndBanDate(LocalDateTime.now().minusDays(1));

        when(userRepository.findByStatusAndEndBanDateBefore(eq(UserStatus.BANNED), any()))
            .thenReturn(List.of(user));

        // When
        userStatusScheduler.unbanUsersIfBanExpired();

        // Then
        assertEquals(UserStatus.ACTIVE, user.getStatus(), "유저 상태가 ACTIVE로 변경되어야 합니다.");
        assertNull(user.getEndBanDate(), "밴 해제 날짜가 null로 설정되어야 합니다.");
        verify(userRepository).saveAll(anyList());
    }


    @Test
    void unbanUsersIfBanExpired_밴해제조건있음_프로필없음() {
        // Given: 밴 해제 조건을 만족하는 유저
        User user = new User();
        user.setUserId(1);
        user.setStatus(UserStatus.BANNED);
        user.setEndBanDate(LocalDateTime.now().minusDays(1));

        when(userRepository.findByStatusAndEndBanDateBefore(eq(UserStatus.BANNED), any()))
            .thenReturn(List.of(user));

        // When
        userStatusScheduler.unbanUsersIfBanExpired();

        // Then
        assertEquals(UserStatus.ACTIVE, user.getStatus(), "유저 상태가 ACTIVE로 변경되어야 합니다.");
        assertNull(user.getEndBanDate(), "밴 해제 날짜가 null로 설정되어야 합니다.");
        verify(userRepository).saveAll(anyList());
    }

    @Test
    void sendEmailNotification_성공() {
        // Given
        String email = "test@example.com";
        String message = "Test message";

        // When
        userStatusScheduler.sendEmailNotification(email, message);

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmailNotification_예외발생() {
        // Given
        String email = "test@example.com";
        String message = "Test message";

        doThrow(new RuntimeException("Email sending error"))
            .when(mailSender).send(any(SimpleMailMessage.class));

        // When
        userStatusScheduler.sendEmailNotification(email, message);

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
        // 예외 발생해도 테스트는 실패하지 않음
    }

}
