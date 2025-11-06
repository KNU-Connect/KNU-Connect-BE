package com.example.knu_connect.domain.auth.service;

import com.example.knu_connect.domain.auth.dto.request.SignupRequestDto;
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.domain.user.entity.enums.*;
import com.example.knu_connect.domain.user.repository.UserRepository;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignupService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Transactional
    public void signup(SignupRequestDto request) {

        // 이메일 인증 여부 확인
        if (!authService.isVerified(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 이메일 중복 체크 (이미 존재하는 이메일)
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // User 엔티티 생성
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(encodedPassword)
                .department(Department.valueOf(request.department()))
                .status(Status.valueOf(request.status()))
                .career(Career.valueOf(request.career()))
                .interest(Interest.valueOf(request.interest()))
                .mbti(Mbti.valueOf(request.mbti()))
                .mentor(request.mentor())
                .build();

        // 저장
        userRepository.save(user);
        log.info("회원가입 성공: {}", request.email());

        // redis에서 인증 상태 삭제
        authService.clearVerifiedEmail(request.email());
    }
}
