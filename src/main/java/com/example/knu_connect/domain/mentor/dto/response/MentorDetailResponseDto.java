package com.example.knu_connect.domain.mentor.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "멘토 상세 정보 응답")
public record MentorDetailResponseDto(
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

        @Schema(description = "간단한 자기소개", example = "안녕하세요. 백엔드 개발에 관심이 많은 멘토입니다.")
        String introduction,

        @Schema(description = "상세 자기소개", example = "저는 5년간 백엔드 개발을 해왔으며, Spring Boot와 JPA를 주로 사용합니다. 현재는 대용량 트래픽 처리와 마이크로서비스 아키텍처에 관심이 많습니다.")
        String detailIntroduction
) {
}
