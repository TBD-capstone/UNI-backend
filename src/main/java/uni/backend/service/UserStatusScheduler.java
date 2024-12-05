package uni.backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uni.backend.domain.User;
import uni.backend.domain.UserStatus;
import uni.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatusScheduler {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;


    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정에 실행
    public void unbanUsersIfBanExpired() {
        log.info("유저 밴 상태 점검을 시작합니다.");

        // 현재 시간 이후로 밴 해제 날짜가 지난 유저를 찾음
        List<User> bannedUsers = userRepository.findByStatusAndEndBanDateBefore(UserStatus.BANNED,
            LocalDateTime.now());

        for (User user : bannedUsers) {
            user.setStatus(UserStatus.ACTIVE);
            user.setEndBanDate(null);

            if (user.getProfile() != null) {
                user.getProfile().setVisible(true);
            }

            log.info("유저 ID={}의 밴 상태가 해제되었습니다.", user.getUserId());
        }

        userRepository.saveAll(bannedUsers);
        log.info("유저 밴 상태 점검이 완료되었습니다.");
    }

    public void sendEmailNotification(String email, String message) {
        try {
            SimpleMailMessage helper = new SimpleMailMessage();
            helper.setFrom("UNI <jdragon@uni-ajou.site>");
            helper.setTo(email);
            helper.setSubject("읽지 않은 메시지가 있습니다.");
            helper.setText(message);
            mailSender.send(helper);
        } catch (Exception e) {
            log.error("Failed to send email to {}", email, e);
        }
    }
}
