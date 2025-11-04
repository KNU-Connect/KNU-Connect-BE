package com.example.knu_connect.unit.auth.jwt;

import com.example.knu_connect.domain.auth.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String secret;

    @BeforeEach
    void init() {
        secret = Base64.getEncoder().encodeToString("mySecretKeymySecretKeymySecretKey12".getBytes()); // 최소 32바이트
        long accessExp = 1800L;  // 30분
        long refreshExp = 3600L; // 1시간

        jwtUtil = new JwtUtil(secret, accessExp, refreshExp);
    }

    @Test
    @DisplayName("AccessToken 생성 성공")
    void createAccessToken_Success() {
        // given
        // when
        String token = jwtUtil.createAccessToken("test@knu.ac.kr");

        // then
        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3); // JWT 구조 확인 (header.payload.signature)
    }

    @Test
    @DisplayName("RefreshToken 생성 성공")
    void createRefreshToken_Success() {
        // given
        // when
        String token = jwtUtil.createRefreshToken("test@knu.ac.kr");

        // then
        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("토큰 검증 성공 및 이메일 추출 성공")
    void getEmail_ValidToken_ReturnsEmail() {
        // given
        String email = "user@test.knu.ac.kr";
        String token = jwtUtil.createAccessToken(email);

        // when
        String result = jwtUtil.getEmail(token);

        // then
        assertThat(result).isEqualTo(email);
    }

    @Test
    @DisplayName("토큰 검증 실패 - 토큰 만료")
    void getEmail_ExpiredToken_ThrowsException() throws InterruptedException {
        // given
        JwtUtil shortLivedJwt = new JwtUtil(secret, 1, 2); // access 1초
        String token = shortLivedJwt.createAccessToken("user@test.knu.ac.kr");
        Thread.sleep(10000); // 토큰 만료

        // when
        ExpiredJwtException exception = assertThrows(ExpiredJwtException.class,
                () -> shortLivedJwt.getEmail(token));

        // then
        assertThat(exception).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("토큰 검증 실패 - 잘못된 토큰")
    void getEmail_InvalidToken_ThrowsException() {
        // given
        String invalidToken = "InvalidToken";

        // when
        JwtException exception = assertThrows(JwtException.class,
                () -> jwtUtil.getEmail(invalidToken));

        // then
        assertThat(exception).isInstanceOf(JwtException.class);
    }
}
