package uni.backend.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer chatRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id")
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id")
    private User user2;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages;

    // 새로운 필드 추가: 마지막 메시지를 보낸 시간
    private LocalDateTime lastMessageAt;

    // 상대방 유저를 반환하는 메서드
    public User getOtherUser(Integer myId) {
        return user1.getUserId().equals(myId) ? user2 : user1;
    }

    // 마지막 메시지 시간을 반환하는 메서드
    public LocalDateTime getLastMessageAt() {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        return messages.get(messages.size() - 1).getSendAt(); // 마지막 메시지의 보낸 시간
    }

    @Builder
    public ChatRoom(User user1, User user2, LocalDateTime createdAt) {
        this.user1 = user1;
        this.user2 = user2;
        this.createdAt = createdAt;
    }
}
