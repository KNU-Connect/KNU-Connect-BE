package com.example.knu_connect.domain.auth.service;

import com.example.knu_connect.domain.auth.dto.request.EmailSendRequestDto;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SpringTemplateEngine templateEngine;

    private static final long CODE_EXPIRE_MINUTES = 5; // 인증 번호 TTL(5분)

    // 이메일 인증 번호 생성 및 전송
    @Async
    public void sendVerificationCode(EmailSendRequestDto requestDto) {

        String email = requestDto.email();
        String code = generateCode();       // 인증 번호 생성 (6자리)

        try {
            // Redis에 코드 저장
            String key = "email:verify:" + email;
            redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

            // 메일 내용 설정
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            mimeMessageHelper.setTo(email); // 메일 수신자
            mimeMessageHelper.setSubject("[KNU-connect] 이메일 인증 번호"); // 메일 제목
            mimeMessageHelper.setText(setContext(code), true); // 메일 본문 내용, HTML 여부

            // 메일 발송
            mailSender.send(mimeMessage);
            log.info("인증 번호 메일 발송 완료: {}", email);
        } catch (Exception e) {
            log.error("메일 발송 또는 Redis 저장 실패", e);
            throw new RuntimeException(e);
        }
    }


    // 인증 번호 생성 (6자리 숫자)
    private String generateCode() {
        int randomCode = (int) (Math.random() * 900000) + 100000; // 6자리
        return String.valueOf(randomCode);
    }

    // thymeleaf 템플릿 적용
    private String setContext(String code) {
        Context context = new Context();
        context.setVariable("code", code);
        return templateEngine.process("email-verification", context);
    }
}
