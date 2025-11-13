package com.example.knu_connect.unit.auth.service.authservice;

import com.example.knu_connect.domain.auth.dto.request.LoginRequestDto;
import com.example.knu_connect.domain.auth.dto.response.TokenWithRefreshResponseDto;
import com.example.knu_connect.global.auth.jwt.CustomUserDetails;
import com.example.knu_connect.global.auth.jwt.JwtUtil;
import com.example.knu_connect.domain.auth.service.AuthService;
import com.example.knu_connect.domain.user.entity.User;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceLoginTest {
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private StringRedisTemplate redisTemplate;
    @InjectMocks private AuthService authService;
    @Mock private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("로그인 성공 - 토큰 발급 및 Redis 저장")
    void login_Success() {
        // given
        LoginRequestDto request = new LoginRequestDto("test@knu.ac.kr", "password");
        User mockUser = mock(User.class);
        when(mockUser.getEmail()).thenReturn("test@knu.ac.kr");

        CustomUserDetails userDetails = new CustomUserDetails(mockUser);
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        when(jwtUtil.createAccessToken("test@knu.ac.kr")).thenReturn("access-token");
        when(jwtUtil.createRefreshToken("test@knu.ac.kr")).thenReturn("refresh-token");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        TokenWithRefreshResponseDto response = authService.login(request);

        // then
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");

        verify(redisTemplate.opsForValue()).set(
                eq("token:refresh:refresh-token"),
                eq("test@knu.ac.kr"),
                anyLong(),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("로그인 실패 - 인증 실패")
    void login_AuthenticationFailed_ThrowsException() {
        // given
        LoginRequestDto request = new LoginRequestDto("wrong@knu.ac.kr", "wrongpw");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new AuthenticationException("Invalid credentials") {});

        // when
        BusinessException exception = assertThrows(BusinessException.class, () ->
                authService.login(request)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTHENTICATION_FAILED);
        assertThat(exception.getMessage()).isEqualTo("이메일 또는 비밀번호가 일치하지 않습니다.");
    }
}
