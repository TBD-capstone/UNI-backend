package uni.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uni.backend.domain.Reply;
import uni.backend.domain.ReplyLikes;
import uni.backend.domain.User;

public interface ReplyLikeRepository extends JpaRepository<ReplyLikes, Long> {

    Optional<ReplyLikes> findByUserAndReply(User user, Reply reply);

}
