package uni.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uni.backend.domain.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
}
