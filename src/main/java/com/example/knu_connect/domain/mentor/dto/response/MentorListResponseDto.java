package com.example.knu_connect.domain.mentor.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "멘토 목록 조회 응답")
public record MentorListResponseDto(
        @Schema(description = "멘토 목록")
        List<MentorDto> mentors,

        @Schema(description = "페이지 번호", example = "0")
        Integer page,

        @Schema(description = "페이지 크기", example = "10")
        Integer size,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        Boolean hasNext
) {
    @Schema(description = "멘토 정보")
    public record MentorDto(
            @Schema(description = "멘토 아이디")
            Long mentorId,

            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "학과", example = "computer")
            String department,

            @Schema(description = "신분", example = "graduate", allowableValues = {"student", "graduate", "postgraduate", "professor"})
            String status,

            @Schema(description = "진로", example = "employment", allowableValues = {"employment", "startup", "matriculation"})
            String career,

            @Schema(description = "관심 분야", example = "backend", allowableValues = {"frontend", "backend", "data", "ai", "security"})
            String interest,

            @Schema(description = "MBTI", example = "ENFP")
            String mbti,

            @Schema(description = "자기소개", example = "안녕하세요. 백엔드 개발에 관심이 많은 멘토입니다.")
            String introduction
    ) {
    }
}
