package com.example.knu_connect.domain.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // 보안 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(form -> form.disable()) // 기본 로그인폼 비활성화
                .httpBasic(basic -> basic.disable()) // HTTP Basic 인증 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll() // 모든 요청 허용 (테스트용)
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    // 비밀번호 암호화용 Bean 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
