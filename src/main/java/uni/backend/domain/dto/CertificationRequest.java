package uni.backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

// 인증 요청 DTO
@Getter
@Setter
public class CertificationRequest {
    private String key;
    private String email;
    private String univName;
    private boolean univCheck;
}

