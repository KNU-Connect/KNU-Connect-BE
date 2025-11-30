package com.example.knu_connect.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 타입 확인 응답")
public record ChatRoomTypeResponseDto(
        @Schema(description = "네트워킹 채팅방 여부", example = "true")
        @JsonProperty("is_networking")
        Boolean isNetworking,

        @Schema(description = "연결된 네트워킹 ID (네트워킹 채팅방일 경우)", example = "1")
        @JsonProperty("networking_id")
        Long networkingId,

        @Schema(description = "네트워킹 제목 (네트워킹 채팅방일 경우)", example = "백엔드 스터디 모집")
        @JsonProperty("networking_title")
        String networkingTitle
) {
}