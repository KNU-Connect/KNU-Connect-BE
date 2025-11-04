package com.example.knu_connect.domain.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;  // 비밀 키
    private final long accessExp;   // 엑세스 토큰 유효시간
    private final long refreshExp;  // 리프레시 토큰 유효시간

    public JwtUtil(@Value("${spring.jwt.secret}") String secret,
                   @Value("${spring.jwt.access-token-expiration}") long accessExp,
                   @Value("${spring.jwt.refresh-token-expiration}") long refreshExp) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessExp = accessExp;
        this.refreshExp = refreshExp;
    }

    // 토큰 생성 (access, refresh)
    public String createAccessToken(String email) {  // 엑세스 토큰 생성
        return createJwt(email, accessExp);
    }

    public String createRefreshToken(String email) { // 리프레시 토큰 생성
        return createJwt(email, refreshExp);
    }

    public String createJwt(String email, long exp) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + exp * 1000L))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // get 메서드 + 유효성 검사도 같이
    public String getEmail(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

}
