package uni.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QnaCreateResponse {

  private String status; // "success" 또는 "fail"
  private String message; // 성공 또는 실패 메시지
  private QnaResponse qna; // 작성된 Qna 정보

  public static QnaCreateResponse success(String message, QnaResponse qna) {
    return new QnaCreateResponse("success", message, qna);
  }

  public static QnaCreateResponse fail(String message) {
    return new QnaCreateResponse("fail", message, null);
  }
}
