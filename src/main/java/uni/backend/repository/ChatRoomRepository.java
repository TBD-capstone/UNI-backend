package uni.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uni.backend.domain.ChatRoom;
import uni.backend.domain.User;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {
    // 두 명의 유저가 참여하는 채팅방을 찾는 쿼리 메서드
    Optional<ChatRoom> findByUser1AndUser2(User user1, User user2);
    Optional<ChatRoom> findByUser2AndUser1(User user2, User user1);
    // user1 또는 user2로 참여한 채팅방 목록을 가져옴
    List<ChatRoom> findByUser1OrUser2(User user1, User user2);
}
