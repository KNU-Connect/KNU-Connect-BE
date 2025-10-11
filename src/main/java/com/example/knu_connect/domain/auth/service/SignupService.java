package com.example.knu_connect.domain.auth.service;

import com.example.knu_connect.domain.auth.dto.request.SignupRequestDto;
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequestDto request) {

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // User 엔티티 생성
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(encodedPassword)
                .department(request.department())
                .status(request.status())
                .career(request.career())
                .interest(request.interest())
                .mbti(request.mbti())
                .mentor(request.mentor())
                .build();

        // 저장
        userRepository.save(user);
    }
}
