package com.example.knu_connect.unit.auth.service.authservice;

import com.example.knu_connect.domain.auth.dto.response.LoginResponseDto;
import com.example.knu_connect.domain.auth.service.AuthService;
import com.example.knu_connect.global.auth.jwt.JwtUtil;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceReissueTokenTest {
    @Mock private JwtUtil jwtUtil;
    @Mock StringRedisTemplate redisTemplate;
    @InjectMocks private AuthService authService;

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissueToken_Success() {
        // given
        String refreshToken = "valid-refresh-token";
        String email = "test@knu.ac.kr";
        String newAccessToken = "new-access-token";

        when(jwtUtil.getEmail(refreshToken)).thenReturn(email);
        when(jwtUtil.getTokenType(refreshToken)).thenReturn(JwtUtil.REFRESH_TOKEN_TYPE);
        when(redisTemplate.hasKey("token:refresh:" + refreshToken)).thenReturn(true);
        when(jwtUtil.createAccessToken(email)).thenReturn(newAccessToken);

        // when
        LoginResponseDto response = authService.reissueToken(refreshToken);

        // then
        assertThat(response.token()).isEqualTo(newAccessToken);

    }

    @Test
    @DisplayName("토큰 재발급 실패 - Refresh Token 없음")
    void reissueToken_NoRefreshToken_ThrowsException() {
        // given
        String refreshToken = null;

        // when
        BusinessException exception = assertThrows(BusinessException.class, () ->
                authService.reissueToken(refreshToken)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
        assertThat(exception.getMessage()).isEqualTo("Refresh Token이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 토큰 만료")
    void reissueToken_ExpiredToken_ThrowsException() {
        // given
        String refreshToken = "expired-token";
        when(jwtUtil.getEmail(refreshToken)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        // when
        BusinessException exception = assertThrows(BusinessException.class, () ->
                authService.reissueToken(refreshToken)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXPIRED_TOKEN);
    }

    @Test
    @DisplayName("토큰 재발급 실패 - Redis에 존재하지 않는 Refresh Token")
    void reissueToken_TokenNotInRedis_ThrowsException() {
        // given
        String refreshToken = "refresh-token";
        when(jwtUtil.getEmail(refreshToken)).thenReturn("test@knu.ac.kr");
        when(jwtUtil.getTokenType(refreshToken)).thenReturn(JwtUtil.REFRESH_TOKEN_TYPE);
        when(redisTemplate.hasKey("token:refresh:" + refreshToken)).thenReturn(false);

        // when
        BusinessException exception = assertThrows(BusinessException.class, () ->
                authService.reissueToken(refreshToken)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
        assertThat(exception.getMessage()).isEqualTo("존재하지 않거나 맞지 않는 토큰입니다.");
    }
}
