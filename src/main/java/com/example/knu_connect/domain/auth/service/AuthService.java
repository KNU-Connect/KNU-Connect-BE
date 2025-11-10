package com.example.knu_connect.domain.auth.service;

import com.example.knu_connect.domain.auth.dto.request.EmailVerifyRequestDto;
import com.example.knu_connect.domain.auth.dto.request.LoginRequestDto;
import com.example.knu_connect.domain.auth.dto.response.TokenWithRefreshResponseDto;
import com.example.knu_connect.global.auth.jwt.CustomUserDetails;
import com.example.knu_connect.global.auth.jwt.JwtUtil;
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final StringRedisTemplate redisTemplate;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private static final long VERIFIED_TTL_MINUTES = 60; // 인증완료 상태 유지시간 (1시간)
    private static final long REFRESH_TTL_MINUTES = 10080; // 리프레시 토큰 유지시간 (7일)

    // 이메일 검증 관련
    // 이메일 코드 검증
    public void verifyCode(EmailVerifyRequestDto requestDto) {
        // request로 받은 정보
        String email = requestDto.email();
        String inputCode = requestDto.verificationCode();

        // Redis에서 해당 이메일의 code 가져오기
        String key = "email:verify:" + email;
        String storedCode = redisTemplate.opsForValue().get(key);

        // 검증
        if (storedCode == null) {   // 이메일이 일치하지 않거나 인증 시간이 만료된 이메일인 경우 (잘못된 요청)
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED, "이메일이 일치하지 않거나 인증 번호가 만료되었습니다.");
        }

        if (!storedCode.equals(inputCode)) {    // 인증번호가 일치하지 않을 경우 (인증번호 불일치)
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 검증 완료
        redisTemplate.delete(key); // 기존 코드 삭제

        try {
            redisTemplate.opsForValue() // redis에 검증된 이메일 저장
                    .set("email:verified:" + email, "true", VERIFIED_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 연결 실패", e);
            throw new BusinessException(ErrorCode.REDIS_CONNECTION_FAILED);
        }


        log.info("이메일 인증 완료: {}", email);
    }

    // 이메일이 인증된 상태인지 확인 (회원가입 시 사용)
    public boolean isVerified(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("email:verified:" + email));
    }

    // Redis에서 verified 키 삭제
    public void clearVerifiedEmail(String email) {
        redisTemplate.delete("email:verified:" + email);
    }

    // 로그인 관련
    // 로그인 요청 처리
    public TokenWithRefreshResponseDto login(LoginRequestDto requestDto) {
        // 인증 (비밀번호 등)
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.email(),
                            requestDto.password()
                    )
            );
        } catch (AuthenticationException ex) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        // 토큰 생성
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        String email = user.getEmail();

        String accessToken = jwtUtil.createAccessToken(email);
        String refreshToken = jwtUtil.createRefreshToken(email);

        // Redis에 Refresh Token 저장
        String key = "email:refresh:" + email;
        redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TTL_MINUTES, TimeUnit.MINUTES);


        return new TokenWithRefreshResponseDto(accessToken, refreshToken);
    }

    // 로그인 시 Refresh Token 쿠키 생성
    public String formatRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/api/auth/refresh")
                .maxAge(Duration.ofDays(7))      // 즉시 만료
                .build()
                .toString();
    }

    // 로그아웃 관련
    // 로그아웃 요청 처리
    public void logout(String accessToken) {

        // 토큰 남은 유효 시간
        long exp = jwtUtil.getExpiration(accessToken) - System.currentTimeMillis();
        if (exp < 0) exp = 0;

        // 인증 정보에서 이메일 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        // Redis에 블랙리스트로 Access Token 저장
        String key = "email:blacklist:" + email;
        redisTemplate.opsForValue().set(key, accessToken, exp, TimeUnit.MILLISECONDS);

        // Redis에 Refresh Token 삭제
        String refreshKey = "email:refresh:" + email;
        redisTemplate.delete(refreshKey);
    }

    // 로그아웃 시 Refresh Token 쿠키 삭제 (만료 쿠키 전송)
    public String formatClearRefreshTokenCookie() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/api/auth/refresh")
                .maxAge(0)      // 즉시 만료
                .build()
                .toString();
    }
}
