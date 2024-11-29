package uni.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uni.backend.domain.Profile;
import uni.backend.domain.Qna;

@Repository
public interface QnaRepository extends JpaRepository<Qna, Integer> {

    // 특정 유저의 QnA 조회
    List<Qna> findByProfileOwnerUserId(Integer userId);

    // 특정 유저의 블라인드 처리된 QnA 조회
    List<Qna> findByCommenter_UserId(Integer userId);

    // 특정 유저의 QnA 블라인드 처리
    @Modifying
    @Query("UPDATE Qna q SET q.isBlind = :isBlind WHERE q.profileOwner.userId = :userId")
    void setBlindStatusByUserId(@Param("userId") Integer userId, @Param("isBlind") boolean isBlind);
}
