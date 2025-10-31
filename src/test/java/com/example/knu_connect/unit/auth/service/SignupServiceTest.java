package com.example.knu_connect.unit.auth.service;

import com.example.knu_connect.domain.auth.dto.request.SignupRequestDto;
import com.example.knu_connect.domain.auth.service.AuthService;
import com.example.knu_connect.domain.auth.service.SignupService;
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.domain.user.repository.UserRepository;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthService authService;
    @InjectMocks private SignupService signupService;

    private SignupRequestDto request;

    @BeforeEach
    void init() {
        request = new SignupRequestDto(
                "홍길동",
                "test@knu.ac.kr",
                "1234",
                "student",
                "computer",
                "employment",
                "ISFP",
                "backend",
                false
        );
    }

    @Test
    @DisplayName("회원가입 성공")
    void Signup_AllValid_Success() {
        // given
        when(authService.isVerified(request.email())).thenReturn(true);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_pw");
        when(userRepository.save(any(User.class))).thenReturn(mock(User.class));

        // when
        signupService.signup(request);

        // then
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(any(User.class));
        verify(authService).clearVerifiedEmail(request.email());
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 미인증")
    void Signup_EmailNotVerified_ThrowsException() {
        // given
        when(authService.isVerified(request.email())).thenReturn(false);

        // when
        BusinessException exception = assertThrows(BusinessException.class, () ->
                signupService.signup(request)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED.message);
        verify(authService, never()).clearVerifiedEmail(anyString());
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void Signup_EmailAlreadyExists_ThrowsException() {
        // given
        when(authService.isVerified(request.email())).thenReturn(true);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // when
        BusinessException exception = assertThrows(BusinessException.class, () ->
                signupService.signup(request)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS.message);
    }
}
