package com.example.knu_connect.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UnreadCountNotificationDto(
        @JsonProperty("chat_room_id")
        Long chatRoomId,

        @JsonProperty("unread_count")
        Long unreadCount
) {
}
