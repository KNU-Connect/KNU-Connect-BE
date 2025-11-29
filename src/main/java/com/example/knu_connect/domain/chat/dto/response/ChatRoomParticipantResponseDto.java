package com.example.knu_connect.domain.chat.dto.response;

import com.example.knu_connect.domain.user.entity.enums.Career;
import com.example.knu_connect.domain.user.entity.enums.Department;
import com.example.knu_connect.domain.user.entity.enums.Interest;
import com.example.knu_connect.domain.user.entity.enums.Mbti;
import io.swagger.v3.oas.annotations.media.Schema;

public record ChatRoomParticipantResponseDto(

        @Schema(description = "유저 ID", example = "1")
        Long userId,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "학과", example = "CSE")
        Department department,

        @Schema(description = "진로", example = "BACKEND")
        Career career,

        @Schema(description = "관심분야", example = "JAVA")
        Interest interest,

        @Schema(description = "MBTI", example = "ESTJ")
        Mbti mbti,

        @Schema(description = "한줄 소개", example = "안녕하세요!")
        String introduction
) {
}
