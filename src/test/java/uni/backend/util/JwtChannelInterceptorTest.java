package uni.backend.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import uni.backend.security.JwtUtils;

class JwtChannelInterceptorTest {

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private JwtChannelInterceptor jwtChannelInterceptor;

    private MessageChannel mockChannel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockChannel = mock(MessageChannel.class);
    }

    @Test
    @DisplayName("유효한 토큰이 있을 때 사용자 설정 성공")
    void validToken_shouldSetAuthenticatedUser() {
        // Given
        String validToken = "Bearer valid.jwt.token";
        when(jwtUtils.validateJwtToken("valid.jwt.token")).thenReturn(true);
        when(jwtUtils.getEmailFromJwtToken("valid.jwt.token")).thenReturn("user@example.com");

        // StompHeaderAccessor 생성
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true); // Mutable 상태 유지
        accessor.addNativeHeader("Authorization", validToken);

        // Message 생성
        Message<?> message = MessageBuilder.createMessage(new byte[0],
            accessor.getMessageHeaders());

        // When
        Message<?> result = jwtChannelInterceptor.preSend(message, mockChannel);

        // Then
        StompHeaderAccessor resultAccessor = MessageHeaderAccessor.getAccessor(result,
            StompHeaderAccessor.class);
        assertNotNull(result);
        assertNotNull(resultAccessor);
        assertEquals("user@example.com",
            Objects.requireNonNull(resultAccessor.getUser()).getName());
    }


    @Test
    @DisplayName("Authorization 헤더가 없을 때 예외 발생")
    void missingAuthorizationHeader_shouldThrowException() {
        // Given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<?> message = MessageBuilder.createMessage(new byte[0],
            accessor.getMessageHeaders());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> jwtChannelInterceptor.preSend(message, mockChannel));
        assertEquals("Authorization header missing or malformed", exception.getMessage());
    }

    @Test
    @DisplayName("Bearer가 없는 헤더일 때 예외 발생")
    void malformedAuthorizationHeader_shouldThrowException() {
        // Given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "InvalidToken");
        Message<?> message = MessageBuilder.createMessage(new byte[0],
            accessor.getMessageHeaders());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> jwtChannelInterceptor.preSend(message, mockChannel));
        assertEquals("Authorization header missing or malformed", exception.getMessage());
    }

    @Test
    @DisplayName("유효하지 않은 토큰일 때 예외 발생")
    void invalidToken_shouldThrowException() {
        // Given
        String invalidToken = "Bearer invalid.jwt.token";
        when(jwtUtils.validateJwtToken("invalid.jwt.token")).thenReturn(false);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", invalidToken);

        Message<?> message = MessageBuilder.createMessage(new byte[0],
            accessor.getMessageHeaders());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> jwtChannelInterceptor.preSend(message, mockChannel));
        assertEquals("Invalid token", exception.getMessage());
    }

    @Test
    @DisplayName("CONNECT가 아닌 메시지는 그대로 통과")
    void nonConnectMessage_shouldPassThrough() {
        // Given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        Message<?> message = MessageBuilder.createMessage(new byte[0],
            accessor.getMessageHeaders());

        // When
        Message<?> result = jwtChannelInterceptor.preSend(message, mockChannel);

        // Then
        assertNotNull(result);
    }
}
