package uni.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uni.backend.domain.ChatMessage;
import uni.backend.domain.ChatRoom;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    List<ChatMessage> findByChatRoom(ChatRoom chatRoom);
}
