package com.example.knu_connect.unit.auth.service;

import com.example.knu_connect.domain.auth.dto.request.EmailSendRequestDto;
import com.example.knu_connect.domain.auth.service.EmailService;
import com.example.knu_connect.domain.user.repository.UserRepository;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Mock private JavaMailSender mailSender;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private SpringTemplateEngine templateEngine;
    @Mock private UserRepository userRepository;
    @InjectMocks private EmailService emailService;

    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private MimeMessage mimeMessage;

    private final EmailSendRequestDto request =
            new EmailSendRequestDto("test@knu.ac.kr");

    @Test
    @DisplayName("이메일 전송 성공")
    void sendVerificationCode_AllValid_Success() {
        // given
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any())).thenReturn("<html>content</html>");

        // when
        emailService.sendVerificationCode(request);

        // then
        verify(redisTemplate.opsForValue()).set(
                startsWith("email:verify:"),
                anyString(),
                eq(5L),
                eq(TimeUnit.MINUTES)
        );
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("이메일 전송 실패 - 전송 오류")
    void SendVerificationCode_MailSendException_ThrowsException() {
        // given
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any())).thenReturn("<html>content</html>");
        doThrow(new MailSendException("fail")).when(mailSender).send(any(MimeMessage.class));

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> emailService.sendVerificationCode(request));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMAIL_SEND_FAILED);
    }

    @Test
    @DisplayName("이메일 전송 실패 - 메시지 구성 오류")
    void SendVerificationCode_MessagingException_ThrowsException() throws Exception{
        // given
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(mailSender.createMimeMessage()).thenAnswer(invocation -> {
            throw new MessagingException("build failed");
        });

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> emailService.sendVerificationCode(request));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMAIL_BUILD_FAILED);
    }

    @Test
    @DisplayName("이메일 전송 실패 - 이미 가입된 이메일")
    void SendVerificationCode_EmailAlreadyExists_ThrowsException() {
        // given
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> emailService.sendVerificationCode(request));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("Redis 저장 실패 - Redis 연결 오류")
    void SendVerificationCode_RedisConnectionFailure_ThrowsException() {
        // given
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RedisConnectionFailureException("fail"))
                .when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> emailService.sendVerificationCode(request));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REDIS_CONNECTION_FAILED);
    }
}
