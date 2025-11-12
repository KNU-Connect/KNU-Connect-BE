package com.example.knu_connect.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageDeletedResponseDto(
        @JsonProperty("deleted_message_id")
        Long deletedMessageId
) {
}
