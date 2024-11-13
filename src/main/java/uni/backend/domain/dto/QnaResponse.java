package uni.backend.domain.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QnaResponse {

    private Integer qnaId;
    private QnaUserResponse profileOwner; // 프로필 주인 정보
    private QnaUserResponse commentAuthor; // 댓글 작성자 정보
    private String content; // 댓글 내용
    private List<ReplyResponse> replies; // 대댓글 리스트
    private String imgProf; // 프로필 주인의 프로필 이미지 URL
    private Boolean deleted; // 삭제 여부
    private String deletedMessage; // 삭제된 경우 메시지
    private Long likes; // 좋아요 수
}
