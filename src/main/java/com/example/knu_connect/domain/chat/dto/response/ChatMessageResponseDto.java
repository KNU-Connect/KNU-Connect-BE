package com.example.knu_connect.domain.chat.dto.response;

import com.example.knu_connect.domain.chat.entitiy.ChatMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record ChatMessageResponseDto(
        @JsonProperty("message_id")
        Long messageId,

        @JsonProperty("sender_id")
        Long senderId,

        @JsonProperty("sender_name")
        String senderName,

        String content,

        @JsonProperty("created_at")
        LocalDateTime createdAt
) {
    public static ChatMessageResponseDto from(ChatMessage message) {
        return new ChatMessageResponseDto(
                message.getId(),
                message.getUserId(),
                message.getUserName(),
                message.getContents(),
                message.getCreatedAt()
        );
    }
}
