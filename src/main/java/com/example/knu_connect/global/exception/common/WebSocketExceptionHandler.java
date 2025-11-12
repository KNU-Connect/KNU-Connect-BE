package com.example.knu_connect.global.exception.common;

import com.example.knu_connect.domain.chat.dto.response.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j
@ControllerAdvice
public class WebSocketExceptionHandler {

    // BusinessException 처리
    @MessageExceptionHandler(BusinessException.class)
    @SendToUser("/queue/errors")
    public ErrorResponseDto handleBusinessException(BusinessException e) {
        log.error("WebSocket BusinessException 발생: {} - {}", 
                e.getErrorCode().code,
                e.getErrorCode().message);

        return ErrorResponseDto.of(
                "BUSINESS_ERROR",
                e.getErrorCode().message,
                e.getErrorCode().code
        );
    }

    // IllegalArgumentException 처리 (인증 실패 등)
    @MessageExceptionHandler(IllegalArgumentException.class)
    @SendToUser("/queue/errors")
    public ErrorResponseDto handleIllegalArgumentException(IllegalArgumentException exception) {
        log.error("WebSocket IllegalArgumentException 발생: {}", exception.getMessage());

        return ErrorResponseDto.of(
                "VALIDATION_ERROR",
                exception.getMessage(),
                "INVALID_ARGUMENT"
        );
    }

    // 모든 예외 처리 (fallback)
    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public ErrorResponseDto handleException(Exception exception) {
        log.error("WebSocket 예상치 못한 에러 발생: {}", exception.getMessage(), exception);

        return ErrorResponseDto.of(
                "UNKNOWN_ERROR",
                "알 수 없는 오류가 발생했습니다",
                "INTERNAL_ERROR"
        );
    }
}
