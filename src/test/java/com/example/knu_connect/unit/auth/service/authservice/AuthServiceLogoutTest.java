package com.example.knu_connect.unit.auth.service.authservice;

import com.example.knu_connect.domain.auth.service.AuthService;
import com.example.knu_connect.global.auth.jwt.JwtUtil;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceLogoutTest {
    @Mock private JwtUtil jwtUtil;
    @Mock StringRedisTemplate redisTemplate;
    @InjectMocks private AuthService authService;
    @Mock ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        // given
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        long expirationTime = System.currentTimeMillis() + 10000; // 10초 후 만료

        when(jwtUtil.getExpiration(accessToken)).thenReturn(expirationTime);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        authService.logout(accessToken, refreshToken);

        // then
        verify(valueOperations).set(
                eq("token:blacklist:" + accessToken),
                eq("true"),
                anyLong(),
                eq(TimeUnit.MILLISECONDS)
        );
        verify(redisTemplate).delete("token:refresh:" + refreshToken);
    }

    @Test
    @DisplayName("로그아웃 실패 - Refresh Token 없음")
    void logout_NoRefreshToken_ThrowsException() {
        // given
        String accessToken = "access-token";
        String refreshToken = null;

        // when
        BusinessException exception = assertThrows(BusinessException.class, () ->
                authService.logout(accessToken, refreshToken)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
        assertThat(exception.getMessage()).isEqualTo("Refresh Token이 존재하지 않습니다.");
    }
}
