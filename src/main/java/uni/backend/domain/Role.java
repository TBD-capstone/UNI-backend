package uni.backend.domain;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ADMIN, KOREAN, EXCHANGE;

    @Override
    public String getAuthority() {
        return name(); // 권한 이름으로 역할 이름 반환
    }
}