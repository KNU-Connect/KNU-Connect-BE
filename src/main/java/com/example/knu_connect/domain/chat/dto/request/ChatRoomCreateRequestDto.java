package com.example.knu_connect.domain.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public record ChatRoomCreateRequestDto (

    @Schema(description = "자신과 대화할 상대의 id를 입력합니다.", example = "1")
    @JsonProperty("participant_id")
    @NotNull(message = "참여자 ID는 필수입니다")
    Long participantId
){}
