package uni.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정에 실행
    public void unbanUsersIfBanExpired() {
        log.info("유저 밴 상태 점검을 시작합니다.");

        // 현재 시간 이후로 밴 해제 날짜가 지난 유저를 찾음
        List<User> bannedUsers = userRepository.findByStatusAndEndBanDateBefore(UserStatus.BANNED,
            LocalDateTime.now());

        for (User user : bannedUsers) {
            user.setStatus(UserStatus.ACTIVE);
            user.setEndBanDate(null);
            log.info("유저 ID={}의 밴 상태가 해제되었습니다.", user.getUserId());
        }

        userRepository.saveAll(bannedUsers);
        log.info("유저 밴 상태 점검이 완료되었습니다.");
    }
}
