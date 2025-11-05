package com.example.knu_connect.global.auth.jwt;

import com.example.knu_connect.global.exception.common.ErrorCode;
import com.example.knu_connect.global.exception.dto.ErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Authorization 헤더가 없거나 Bearer 토큰이 아닐 경우 패스
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 추출
        String accessToken = header.split(" ")[1];
        try {
            // 토큰 검증 및 이메일 추출
            String email = jwtUtil.getEmail(accessToken);

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
        } catch (Exception e) {
            log.error("JWT 처리 중 알 수 없는 오류 발생: {}", e.getMessage(), e);
            jwtExceptionHandler(response, ErrorCode.UNKNOWN_ERROR, request.getRequestURI());
        }
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
