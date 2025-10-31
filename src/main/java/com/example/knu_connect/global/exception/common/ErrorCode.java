package com.example.knu_connect.global.exception.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),
    JSON_PARSE_ERROR(HttpStatus.BAD_REQUEST, "JSON_PARSE_ERROR", "요청 본문을 해석할 수 없습니다."),

    // Domain Common
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", "요청한 대상을 찾을 수 없습니다."),
    DUPLICATE_KEY(HttpStatus.CONFLICT, "DUPLICATE_KEY", "이미 존재하는 값입니다."),

    // Auth
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "EMAIL_NOT_VERIFIED", "이메일 인증이 완료되지 않았습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 가입된 이메일입니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "INVALID_VERIFICATION_CODE", "인증번호가 일치하지 않습니다."),
    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "VERIFICATION_CODE_EXPIRED", "인증번호가 만료되었습니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_SEND_FAILED", "이메일 전송 실패"),
    EMAIL_BUILD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_BUILD_FAILED", "이메일 메시지 생성 실패"),
    REDIS_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS_CONNECTION_FAILED", "Redis 연결 실패");

    // User

    // Chat

    // Schedule

    public final HttpStatus status;
    public final String code;
    public final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
