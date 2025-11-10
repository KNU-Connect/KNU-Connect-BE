package com.example.knu_connect.global.auth.jwt;

import com.example.knu_connect.global.config.SecurityConfig;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import com.example.knu_connect.global.exception.dto.ErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final StringRedisTemplate redisTemplate;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 인증 제외 조건이면 필터 통과
        if (isExcludedAuthentication(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 추출
        String accessToken;
        String[] parts = request.getHeader("Authorization").split(" ");
        if (parts.length != 2) {
            accessToken = "invalidToken";
        } else {
            accessToken = parts[1];
        }

        try {
            // 토큰 파싱하여 claim 가지고 오기 (검증도 같이)
            Claims claims = jwtUtil.getClaims(accessToken);
            String tokenType = claims.get(JwtUtil.TOKEN_TYPE_CLAIM, String.class);
            String email = claims.getSubject();

            // 액세스 토큰인지 확인
            if (!JwtUtil.ACCESS_TOKEN_TYPE.equals(tokenType)) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN_TYPE);
            }

            // 로그아웃한 토큰인지 확인
            String blacklistedToken = redisTemplate.opsForValue().get("email:blacklist:" + email);
            if (accessToken.equals(blacklistedToken)) {
                throw new BusinessException(ErrorCode.BLACKLIST_TOKEN);
            }

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(email);

                // 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // SecurityContextHolder에 인증 정보 등록
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.info("만료된 토큰 요청: {}", request.getRequestURI());
            jwtExceptionHandler(response, ErrorCode.EXPIRED_TOKEN, request.getRequestURI());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 토큰 요청: {}", request.getRequestURI());
            jwtExceptionHandler(response, ErrorCode.INVALID_TOKEN, request.getRequestURI());
        } catch (UsernameNotFoundException e) {
            log.warn("JWT 인증 중 사용자 정보를 찾을 수 없음: {}", e.getMessage());
            jwtExceptionHandler(response, ErrorCode.USER_NOT_FOUND, request.getRequestURI());
        } catch (BusinessException e) {
            log.warn("비즈니스 예외 발생 [{}]: {}", e.getErrorCode().name(), e.getMessage());
            jwtExceptionHandler(response, e.getErrorCode(), request.getRequestURI());
        } catch (Exception e) {
            log.error("JWT 처리 중 알 수 없는 오류 발생: {}", e.getMessage(), e);
            jwtExceptionHandler(response, ErrorCode.UNKNOWN_ERROR, request.getRequestURI());
        }
    }

    private boolean isExcludedAuthentication(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        // 로그인 불필요한 경로
        for (String pattern : SecurityConfig.ALLOWED_URLS) {
            if (pathMatcher.match(pattern, uri)) {
                return true;
            }
        }

        // Authorization 헤더가 없거나 Bearer 형식이 아님
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return true;
        }

        return false;
    }

    private void jwtExceptionHandler(HttpServletResponse response, ErrorCode errorCode, String path) {
        response.setStatus(errorCode.status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            ErrorResponseDto body = ErrorResponseDto.of(
                    errorCode,
                    errorCode.message,              // 기본 메시지 사용
                    Collections.emptyList(),
                    path
            );

            ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            String json = mapper.writeValueAsString(body);
            response.getWriter().write(json);

        } catch (IOException e) {
            log.error("JWT 예외 응답 처리 중 오류 발생: {}", e.getMessage());
        }
    }
}
