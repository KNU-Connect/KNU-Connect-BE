package com.example.knu_connect.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatRoomCreateResponseDto(
        @JsonProperty("chat_room_id")
        Long chatRoomId
) {
}
