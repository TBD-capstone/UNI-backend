package uni.backend.domain.dto;


import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReviewResponse {

    private Integer matchingId;
    private Integer reviewId;
    private Integer profileOwnerId;
    private String profileOwnerName;
    private Integer commenterId;
    private String commenterName;
    private String commenterImgProf; // 작성자의 프로필 이미지 추가
    private String content;
    private Integer star;
    private Long likes;
    private Boolean deleted;
    private LocalDateTime deletedTime;
    private LocalDateTime updatedTime;
    private String deleteMessage;
    private List<ReviewReplyResponse> replies;

}
