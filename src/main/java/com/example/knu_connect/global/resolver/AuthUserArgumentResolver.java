package com.example.knu_connect.global.resolver;

import com.example.knu_connect.global.auth.jwt.CustomUserDetails;
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.global.annotation.AuthUser;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 파라미터에 @AuthUser가 붙어있고, 타입이 User이면 지원
        return parameter.hasParameterAnnotation(AuthUser.class)
                && User.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS); // 401 예외
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails customUserDetails)) {
            throw new BusinessException(ErrorCode.INVALID_AUTH_PRINCIPAL);
        }

        return customUserDetails.getUser();
    }
}
