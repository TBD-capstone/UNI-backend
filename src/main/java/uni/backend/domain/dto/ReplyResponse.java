package uni.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReplyResponse {

  private Integer replyId;
  private Integer commenterId; // 댓글 작성자 ID
  private String content; // 대댓글 내용
  private Integer qnaId; // 관련 Qna ID
  private String imgProf; // 댓글 작성자의 프로필 이미지 URL
  private Boolean deleted; // 삭제 여부
  private String deletedMessage; // 삭제된 경우 메시지
  private Long likes; // 좋아요 수 추가
}
