package com.example.knu_connect.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 정보 조회 응답")
public record UserInfoResponseDto(

        @Schema(description = "아이디", example = "1L")
        Long id,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "신분", example = "student", allowableValues = {"student", "graduate", "postgraduate", "professor"})
        String status,

        @Schema(description = "학과", example = "computer")
        String department,

        @Schema(description = "진로", example = "employment", allowableValues = {"employment", "startup", "matriculation"})
        String career,

        @Schema(description = "MBTI", example = "ENFP")
        String mbti,

        @Schema(description = "관심 분야", example = "backend", allowableValues = {"frontend", "backend", "data", "ai", "security"})
        String interest,

        @Schema(description = "멘토 여부", example = "true")
        Boolean mentor,

        @Schema(description = "간단한 자기소개", example = "안녕하세요. 백엔드 개발에 관심이 많은 학생입니다.")
        String introduction,

        @JsonProperty("detail_introduction")
        @Schema(description = "상세 자기소개", example = "저는 Spring Boot를 활용한 백엔드 개발에 관심이 많으며, 현재 여러 프로젝트를 진행하고 있습니다.")
        String detailIntroduction
) {
}
