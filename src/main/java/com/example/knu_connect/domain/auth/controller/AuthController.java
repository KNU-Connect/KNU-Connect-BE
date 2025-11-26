package com.example.knu_connect.domain.auth.controller;

import com.example.knu_connect.domain.auth.dto.request.EmailSendRequestDto;
import com.example.knu_connect.domain.auth.dto.request.EmailVerifyRequestDto;
import com.example.knu_connect.domain.auth.dto.request.LoginRequestDto;
import com.example.knu_connect.domain.auth.dto.request.SignupRequestDto;
import com.example.knu_connect.domain.auth.dto.response.EmailResponseDto;
import com.example.knu_connect.domain.auth.dto.response.LoginResponseDto;
import com.example.knu_connect.domain.auth.dto.response.TokenWithRefreshResponseDto;
import com.example.knu_connect.domain.auth.service.AuthService;
import com.example.knu_connect.domain.auth.service.EmailService;
import com.example.knu_connect.domain.auth.service.SignupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "01. Auth", description = "인증/인가 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignupService signupService;
    private final EmailService emailService;
    private final AuthService authService;

    @Operation(
            summary = "회원가입",
            description = """
                새로운 사용자를 등록합니다.
                
                각 필드에 사용 가능한 값은 다음과 같습니다.
                * status: student, graduate, postgraduate, professor 중 선택
                * department: computer << 현재는 컴퓨터학부 대상으로만 서비스하며 추후 확장 가능합니다.
                * career: employment, startup, matriculation
                * mbti: ISTJ, ISFJ, INFJ, INTJ, ISTP, ISFP, INFP, INTP, ESTP, ESFP, ENFP, ENTP, ESTJ, ESFJ, ENFJ, ENTJ, NONE 중 선택
                * interest: frontend, backend, data, ai, security 중 선택
                """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일", content = @Content)
    })
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequestDto request) {
        signupService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        // 로그인 요청 처리하고 Access/Refresh Token 발급
        TokenWithRefreshResponseDto tokens = authService.login(request);

        // Refresh Token을 쿠키(Set-Cookie 헤더) 형식의 문자열로 반환
        String setCookieValue = authService.formatRefreshTokenCookie(tokens.refreshToken());

        // Access Token은 본문으로, Refresh Token은 쿠키로 반환
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, setCookieValue)
                .body(new LoginResponseDto(tokens.accessToken()));
    }

    @Operation(
            summary = "인증 토큰 재발급", 
            description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "토큰 재발급 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰", content = @Content),
            @ApiResponse(responseCode = "403", description = "만료된 Refresh Token", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(@CookieValue(value = "refresh_token", required = false) String refreshToken) {
        // Refresh Token 검증하고 새 Access Token 발급
        LoginResponseDto response = authService.reissueToken(refreshToken);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "로그아웃", 
            description = "현재 로그인된 사용자를 로그아웃 처리합니다",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
    })
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "refresh_token", required = false) String refreshToken,
                                       @RequestHeader(value = "Authorization") String authorization) {

        // Redis에 Access Token 블랙리스트 등록 및 Refresh Token 삭제 처리
        String accessToken = authorization.split(" ")[1];
        authService.logout(accessToken, refreshToken);

        // Refresh Token 쿠키를 무효화(삭제용 Set-Cookie 헤더 생성)
        String clearCookieValue = authService.formatClearRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookieValue)
                .build();
    }

    @Operation(summary = "이메일 인증번호 전송", description = "입력한 이메일로 인증번호를 전송합니다")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "인증번호 전송 성공",
                    content = @Content(schema = @Schema(implementation = EmailResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "500", description = "이메일 전송 실패", content = @Content)
    })
    @PostMapping("/email/send")
    public ResponseEntity<EmailResponseDto> sendEmailVerification(@Valid @RequestBody EmailSendRequestDto request) {
        emailService.sendVerificationCode(request);
        EmailResponseDto response = new EmailResponseDto(true, "인증번호가 전송되었습니다");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "이메일 인증번호 확인", description = "전송된 인증번호를 확인합니다")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "인증 확인 완료",
                    content = @Content(schema = @Schema(implementation = EmailResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 인증번호 불일치", content = @Content)
    })
    @PostMapping("/email/verify")
    public ResponseEntity<EmailResponseDto> verifyEmail(@Valid @RequestBody EmailVerifyRequestDto request) {
        authService.verifyCode(request);
        EmailResponseDto response = new EmailResponseDto(true, "이메일 인증이 완료되었습니다");
        return ResponseEntity.ok(response);
    }
}
