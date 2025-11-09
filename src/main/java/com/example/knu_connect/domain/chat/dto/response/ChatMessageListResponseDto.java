package com.example.knu_connect.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


public record ChatMessageListResponseDto(

    @JsonProperty("user_id")
    Long userId,
    List<ChatMessageResponseDto> messages,
    Boolean hasNext,
    Long nextCursor
) {}
