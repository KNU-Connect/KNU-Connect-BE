package com.example.knu_connect.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "이메일 인증번호 전송 요청")
public record EmailSendRequestDto(
        @Schema(description = "이메일", example = "user@knu.ac.kr")
        @NotBlank(message = "이메일은 필수입니다")
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@knu.ac.kr$", message = "경북대학교 이메일만 사용할 수 있습니다.")
        String email
) {
}
