package com.example.knu_connect.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record ChatRoomListResponseDto(
        @JsonProperty("chat_rooms")
        List<ChatRoomInfo> chatRooms
) {
    public record ChatRoomInfo(
            Long id,
            String title,

            @JsonProperty("unread_count")
            Long unreadCount,

            @JsonProperty("recent_message")
            String recentMessage,

            @JsonProperty("recent_date")
            LocalDateTime recentDate
    ) {
    }
}
