package uni.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uni.backend.domain.Qna;
import uni.backend.domain.Reply;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Integer> {

    List<Reply> findByCommenter_UserId(Integer userId); // 작성자 ID로 답글 조회


}