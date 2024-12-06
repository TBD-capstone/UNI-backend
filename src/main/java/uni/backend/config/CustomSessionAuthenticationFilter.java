package uni.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CustomSessionAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 요청 헤더에서 세션 ID 가져오기
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Session ")) {
            String sessionId = authorizationHeader.substring(8); // "Session " 이후 추출

            // 세션에서 SecurityContext 가져오기
            HttpSession session = request.getSession(false);
            if (session != null && sessionId.equals(session.getId())) {
                SecurityContext securityContext =
                        (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");

                if (securityContext != null) {
                    // SecurityContext를 설정
                    SecurityContextHolder.setContext(securityContext);
                }
            }
        }

        // 다음 필터로 이동
        filterChain.doFilter(request, response);
    }
}
