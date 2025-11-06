package com.example.knu_connect.global.auth.jwt;

import io.jsonwebtoken.Claims;
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

    // 토큰 타입을 저장할 Claim 키와 토큰 타입 값
    public static final String TOKEN_TYPE_CLAIM = "tokenType";
    public static final String ACCESS_TOKEN_TYPE = "access";
    public static final String REFRESH_TOKEN_TYPE = "refresh";

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
        return createJwt(email, accessExp, ACCESS_TOKEN_TYPE);
    }

    public String createRefreshToken(String email) { // 리프레시 토큰 생성
        return createJwt(email, refreshExp, REFRESH_TOKEN_TYPE);
    }

    public String createJwt(String email, long exp, String tokenType) {
        return Jwts.builder()
                .subject(email)
                .claim(TOKEN_TYPE_CLAIM, tokenType) // 토큰 타입을 지정하여 액세스인지, 리프레쉬 토큰인지 확인하도록 함
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + exp * 1000L))
                .signWith(secretKey)
                .compact();
    }

    // get 메서드 + 유효성 검사도 같이
    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    // 토큰 타입 추출
    public String getTokenType(String token) {
        return getClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
    }

    // 토큰 검증 수행하면서 모든 Claims 추출
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
