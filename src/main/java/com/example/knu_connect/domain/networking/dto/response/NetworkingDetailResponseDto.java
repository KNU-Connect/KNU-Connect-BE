package com.example.knu_connect.domain.networking.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "네트워킹 상세 정보 응답")
public record NetworkingDetailResponseDto(
        @Schema(description = "게시물 ID", example = "1")
        Long id,

        @Schema(description = "제목", example = "백엔드 개발자 스터디 모집")
        String title,

        @Schema(description = "내용", example = "Spring Boot를 활용한 백엔드 개발 스터디원을 모집합니다.")
        String contents,

        @Schema(description = "현재 인원", example = "3")
        Integer curNumber,

        @Schema(description = "최대 인원", example = "5")
        Integer maxNumber,

        @Schema(description = "참여 여부", example = "false")
        Boolean isParticipating,

        @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "대표자 정보")
        RepresentativeDto representative
) {
    @Schema(description = "대표자 정보")
    public record RepresentativeDto(

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
