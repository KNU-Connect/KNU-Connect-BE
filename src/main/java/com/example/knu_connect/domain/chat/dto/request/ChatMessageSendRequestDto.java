package com.example.knu_connect.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public record ChatMessageSendRequestDto(

        @Schema(description = "메세지 전송 시 내용을 입력합니다.", example = "안녕하세요.")
        @NotBlank(message = "메시지 내용은 필수입니다")
        String content
) {}
