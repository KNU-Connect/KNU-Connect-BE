package com.example.knu_connect.domain.auth.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final String[] allowedUrls = {"/h2-console/**", "/swagger-ui/**", "/api-docs/**", "/actuator/health",
            "/api/auth/signup", "/api/auth/login", "/api/auth/email/**", "api/auth/refresh"}; // 인증 없이 허용할 url 목록

    // 보안 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // csrf 비활성화
                .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인폼 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(allowedUrls).permitAll()
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)); // h2 콘솔은 iframe 허용

        return http.build();
    }

    // 비밀번호 암호화용 Bean 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
