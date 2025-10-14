package com.example.knu_connect.domain.auth.service;

import com.example.knu_connect.domain.auth.dto.request.EmailVerifyRequestDto;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
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
        if (storedCode == null) {   // 이메일이 일치하지 않거나 인증 시간이 만료된 이메일인 경우 (잘못된 요청)
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED, "이메일이 일치하지 않거나 인증 번호가 만료되었습니다.");
        }

        if (!storedCode.equals(inputCode)) {    // 인증번호가 일치하지 않을 경우 (인증번호 불일치)
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
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

    // redis에서 verified 키 삭제
    public void clearVerifiedEmail(String email) {
        redisTemplate.delete("email:verified:" + email);
    }
}
