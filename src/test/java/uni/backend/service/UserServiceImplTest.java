package uni.backend.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import uni.backend.domain.User;
import uni.backend.repository.UserRepository;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void generateAndSendResetCode_ShouldSendEmail_WhenUserExists() {
        // given
        String email = "test@example.com";
        User mockUser = new User();
        mockUser.setEmail(email);

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        // when
        userService.generateAndSendResetCode(email);

        // then
        Mockito.verify(userRepository, Mockito.times(1)).findByEmail(email);
        Mockito.verify(mailSender, Mockito.times(1))
                .send(Mockito.<SimpleMailMessage>argThat(msg ->
                        msg.getTo()[0].equals(email) &&
                                msg.getSubject().equals("Password Reset Code") &&
                                msg.getText().startsWith("Your password reset code is:")
                ));
    }

    @Test
    void generateAndSendResetCode_ShouldThrowException_WhenUserDoesNotExist() {
        // given
        String email = "nonexistent@example.com";

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userService.generateAndSendResetCode(email);
        });

        Assertions.assertEquals("No user found with email: " + email, exception.getMessage());
        Mockito.verify(mailSender, Mockito.never()).send(Mockito.any(SimpleMailMessage.class));
    }
}
