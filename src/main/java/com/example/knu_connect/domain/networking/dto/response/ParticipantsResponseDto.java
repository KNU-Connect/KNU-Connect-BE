package com.example.knu_connect.domain.networking.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "네트워킹 참여자 목록 응답")
public record ParticipantsResponseDto(
        @Schema(description = "참여자 목록")
        List<ParticipantDto> participants
) {
    @Schema(description = "참여자 정보")
    public record ParticipantDto(
            @Schema(description = "아이디", example = "1L")
            Long id,

            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "신분", example = "student")
            String status,

            @Schema(description = "학과", example = "computer")
            String department,

            @Schema(description = "진로", example = "employment")
            String career,

            @Schema(description = "MBTI", example = "ENFP")
            String mbti,

            @Schema(description = "관심 분야", example = "backend")
            String interest,

            @Schema(description = "자기소개", example = "안녕하세요. 백엔드 개발에 관심이 많은 학생입니다.")
            String introduction
    ) {
    }
}
