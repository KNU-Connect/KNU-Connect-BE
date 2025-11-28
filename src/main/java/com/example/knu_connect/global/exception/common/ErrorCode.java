package com.example.knu_connect.global.exception.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),
    JSON_PARSE_ERROR(HttpStatus.BAD_REQUEST, "JSON_PARSE_ERROR", "요청 본문을 해석할 수 없습니다."),
    INVALID_AUTH_PRINCIPAL(HttpStatus.INTERNAL_SERVER_ERROR, "INVALID_AUTH_PRINCIPAL", "인증 객체가 잘못된 타입입니다."),

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
    REDIS_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS_CONNECTION_FAILED", "Redis 연결 실패"),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "로그인 인증 실패"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "UNKNOWN_ERROR", "예상치 못한 오류가 발생했습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_ACCESS", "액세스 토큰이 필요합니다."),
    INVALID_TOKEN_TYPE(HttpStatus.BAD_REQUEST, "INVALID_TOKEN_TYPE", "잘못된 토큰 타입입니다."),
    BLACKLIST_TOKEN(HttpStatus.UNAUTHORIZED, "BLACKLIST_TOKEN", "로그아웃된 토큰입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),

    // Mentor
    MENTOR_NOT_FOUND(HttpStatus.NOT_FOUND, "MENTOR_NOT_FOUND", "멘토를 찾을 수 없습니다."),

    // Chat
    FORBIDDEN_DELETE_MESSAGE(HttpStatus.FORBIDDEN, "FORBIDDEN_DELETE_MESSAGE", "채팅메세지를 삭제할 권한이 없습니다."),
    CHAT_PARTICIPANTS_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_PARTICIPANTS_NOT_FOUND", "채팅방 참여자가 아닙니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_MESSAGE_NOT_FOUND", "채팅 메세지를 찾을 수 없습니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다."),

    // Networking
    INVALID_MAX_NUMBER(HttpStatus.BAD_REQUEST, "INVALID_MAX_NUMBER", "네트워킹 최대 참여 한도가 현재 참여 수보다 작을 수 없습니다."),
    NETWORKING_FORBIDDEN(HttpStatus.FORBIDDEN, "NETWORKING_FORBIDDEN", "네트워킹 대표만이 사용 가능합니다."),
    NETWORKING_NOT_FOUND(HttpStatus.NOT_FOUND, "NETWORKING_NOT_FOUND", "네트워킹을 찾을 수 없습니다."),
    NETWORKING_FULL(HttpStatus.BAD_REQUEST, "NETWORKING_FULL", "모집 인원이 마감되었습니다."),
    ALREADY_PARTICIPATED(HttpStatus.CONFLICT, "ALREADY_PARTICIPATED", "이미 참여 중인 네트워킹입니다.");

    public final HttpStatus status;
    public final String code;
    public final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
