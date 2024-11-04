package uni.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uni.backend.domain.ChatRoom;
import uni.backend.domain.User;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {

    Optional<ChatRoom> findBySenderAndReceiver(User sender, User receiver);

    List<ChatRoom> findBySenderOrReceiver(User sender,
        User receiver); // 사용자(sender 또는 receiver)에 관련된 채팅방 모두 조회
}
