package com.example.knu_connect.unit.auth.service.authservice;

import com.example.knu_connect.domain.auth.dto.request.EmailVerifyRequestDto;
import com.example.knu_connect.domain.auth.service.AuthService;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceSignupTest {
    @Mock private RedisTemplate<String, String> redisTemplate;
    @InjectMocks private AuthService authService;

    @Mock private ValueOperations<String, String> valueOperations;

    private final EmailVerifyRequestDto request =
            new EmailVerifyRequestDto("test@knu.ac.kr", "123456");

    @Test
    @DisplayName("이메일 인증 성공")
    void verifyCode_AllValid_Success() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("email:verify:test@knu.ac.kr")).thenReturn("123456");

        // when
        authService.verifyCode(request);

        // then
        verify(redisTemplate).delete("email:verify:test@knu.ac.kr");
        verify(valueOperations).set(
                eq("email:verified:test@knu.ac.kr"),
                eq("true"),
                eq(60L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("이메일 인증 실패 - 인증번호 불일치")
    void verifyCode_InvalidCode_ThrowsException() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("email:verify:test@knu.ac.kr")).thenReturn("999999");

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.verifyCode(request));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_VERIFICATION_CODE);
    }

    @Test
    @DisplayName("이메일 인증 실패 - 코드 만료(null 반환)")
    void verifyCode_Expired_ThrowsException() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("email:verify:test@knu.ac.kr")).thenReturn(null);

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.verifyCode(request));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VERIFICATION_CODE_EXPIRED);
    }

    @Test
    @DisplayName("이메일 인증 실패 - Redis 연결 오류")
    void verifyCode_RedisConnectionFailure_ThrowsException() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("email:verify:test@knu.ac.kr")).thenReturn("123456");
        doThrow(new RedisConnectionFailureException("fail"))
                .when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.verifyCode(request));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REDIS_CONNECTION_FAILED);
    }

    @Test
    @DisplayName("이메일 인증 상태 확인 - 키 존재 시 true 반환")
    void isVerified_ReturnsTrue() {
        // given
        when(redisTemplate.hasKey("email:verified:test@knu.ac.kr")).thenReturn(true);

        // when
        boolean result = authService.isVerified("test@knu.ac.kr");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이메일 인증 상태 확인 - 키 미존재 시 false 반환")
    void isVerified_ReturnsFalse() {
        // given
        when(redisTemplate.hasKey("email:verified:test@knu.ac.kr")).thenReturn(false);

        // when
        boolean result = authService.isVerified("test@knu.ac.kr");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("이메일 인증 상태 초기화 - verified 키 삭제")
    void clearVerifiedEmail_Success() {
        // given
        // when
        authService.clearVerifiedEmail("test@knu.ac.kr");

        // then
        verify(redisTemplate).delete("email:verified:test@knu.ac.kr");
    }

}
