package uni.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer chatRoomId;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender; // user1

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver; // user2

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatMessage> chatMessages;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
