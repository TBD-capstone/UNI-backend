package uni.backend.domain.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Integer userId;
    private String email;
    private String name;
    private String status;
    private String univName;
    private String role;
    private String lastReportReason;
    private Long reportCount;
    private LocalDateTime endBanDate;
}