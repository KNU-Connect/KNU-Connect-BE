package com.example.knu_connect.unit.global;

import com.example.knu_connect.global.auth.jwt.CustomUserDetails;
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.global.annotation.AuthUser;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import com.example.knu_connect.global.resolver.RestAuthUserArgumentResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUserArgumentResolverTest {

    private RestAuthUserArgumentResolver resolver;

    @Mock private MethodParameter parameter;

    @BeforeEach
    void init() {
        resolver = new RestAuthUserArgumentResolver();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void afterEach() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("파라미터 조건 맞음")
    void supportsParameter_AllValid_ReturnsTrue() {
        // given
        when(parameter.hasParameterAnnotation(AuthUser.class)).thenReturn(true);
        when(parameter.getParameterType()).thenReturn((Class) User.class);

        // when
        boolean result = resolver.supportsParameter(parameter);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("파라미터 조건 틀림 - User.class가 아님")
    void supportsParameter_NotUserType_ReturnsFalse() {
        // given
        when(parameter.hasParameterAnnotation(AuthUser.class)).thenReturn(true);
        when(parameter.getParameterType()).thenReturn((Class) String.class);

        // when
        boolean result = resolver.supportsParameter(parameter);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("resolveArgument 성공 - User 반환")
    void resolveArgument_Authenticated_ReturnsUser() throws Exception {
        // given
        User mockUser = mock(User.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUser()).thenReturn(mockUser);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        Object result = resolver.resolveArgument(parameter, null, null, null);

        // then
        assertThat(result).isEqualTo(mockUser);
    }

    @Test
    @DisplayName("resolveArgument 실패 - 인증 정보 없음")
    void resolveArgument_NoAuthentication_ThrowsException() {
        // given
        SecurityContextHolder.clearContext(); // 인증 정보 없음

        // when
        BusinessException exception = assertThrows(BusinessException.class, () ->
                resolver.resolveArgument(parameter, null, null, null)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_ACCESS);
    }

    @Test
    @DisplayName("resolveArgument 실패 - Principal이 CustomUserDetails가 아님")
    void resolveArgument_PrincipalNotCustomUserDetails_ThrowsException() {
        // given
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("notCustomUserDetails", null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        BusinessException exception = assertThrows(BusinessException.class, () ->
                resolver.resolveArgument(parameter, null, null, null)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_AUTH_PRINCIPAL);
    }
}
