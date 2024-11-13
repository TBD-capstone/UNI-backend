package uni.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uni.backend.domain.Qna;
import uni.backend.domain.Reply;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Integer> {

}