package com.example.knu_connect.domain.auth.service;

import com.example.knu_connect.domain.auth.dto.request.EmailVerifyRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final StringRedisTemplate redisTemplate;
    private static final long VERIFIED_TTL_MINUTES = 60; // 인증완료 상태 유지시간 (1시간)

    public void verifyCode(EmailVerifyRequestDto requestDto) {
        // request로 받은 정보
        String email = requestDto.email();
        String inputCode = requestDto.verificationCode();

        // redis에서 해당 이메일의 code 가져오기
        String key = "email:verify:" + email;
        String storedCode = redisTemplate.opsForValue().get(key);

        // 검증
        if (storedCode == null) {   // 이메일이 일치하지 않거나 인증 시간이 만료된 이메일인 경우
            throw new IllegalArgumentException("일치하지 않거나 인증 시간이 만료된 이메일입니다. 다시 요청해주세요.");
        }

        if (!storedCode.equals(inputCode)) {    // 인증번호가 일치하지 않을 경우
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        // 검증 완료
        redisTemplate.delete(key); // 기존 코드 삭제
        redisTemplate.opsForValue() // redis에 검증된 이메일 저장
                .set("email:verified:" + email, "true", VERIFIED_TTL_MINUTES, TimeUnit.MINUTES);

        log.info("이메일 인증 완료: {}", email);
    }

    // 이메일이 인증된 상태인지 확인 (회원가입 시 사용)
    public boolean isVerified(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("email:verified:" + email));
    }
}
