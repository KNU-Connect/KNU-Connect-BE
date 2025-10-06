package com.example.knu_connect.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 인증 응답")
public record EmailResponseDto(
        @Schema(description = "성공 여부", example = "true")
        Boolean success,

        @Schema(description = "메시지", example = "인증번호가 전송되었습니다")
        String message
) {
}
