package uni.backend.domain.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewReplyResponse {

    private Integer replyId; // 대댓글 ID
    private Integer reviewId; // 연결된 리뷰 ID
    private Integer commenterId; // 작성자 ID
    private String commenterName; // 작성자 이름
    private String commenterImgProf; // 작성자 프로필 이미지
    private String content; // 대댓글 내용
    private Long likes; // 좋아요 수
    private Boolean deleted; // 삭제 여부
    private LocalDateTime deletedTime; // 삭제 시간
    private String deleteMessage;
    private LocalDateTime updatedTime; // 수정 시간
}