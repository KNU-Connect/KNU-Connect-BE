package com.example.knu_connect.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "유저 정보 수정 요청")
public record UserUpdateRequestDto(
        @Schema(description = "학과", example = "computer", allowableValues = {"computer"})
        @NotBlank(message = "학과는 필수입니다")
        @Pattern(regexp = "^computer$", message = "현재는 computer 학과만 지원합니다")
        String department,

        @Schema(description = "진로", example = "employment", allowableValues = {"employment", "startup", "matriculation"})
        @NotBlank(message = "진로는 필수입니다")
        @Pattern(regexp = "^(employment|startup|matriculation)$", message = "진로는 employment, startup, matriculation 중 하나여야 합니다")
        String career,

        @Schema(description = "MBTI", example = "ENFP")
        @NotBlank(message = "MBTI는 필수입니다")
        @Pattern(regexp = "^(ISTJ|ISFJ|INFJ|INTJ|ISTP|ISFP|INFP|INTP|ESTP|ESFP|ENFP|ENTP|ESTJ|ESFJ|ENFJ|ENTJ)$",
                message = "올바른 MBTI 유형이 아닙니다")
        String mbti,

        @Schema(description = "신분", example = "student", allowableValues = {"student", "graduate", "postgraduate", "professor"})
        @NotBlank(message = "신분은 필수입니다")
        @Pattern(regexp = "^(student|graduate|postgraduate|professor)$", message = "신분은 student, graduate, postgraduate, professor 중 하나여야 합니다")
        String status,

        @Schema(description = "관심 분야", example = "backend", allowableValues = {"frontend", "backend", "data", "ai", "security"})
        @NotBlank(message = "관심 분야는 필수입니다")
        @Pattern(regexp = "^(frontend|backend|data|ai|security)$", message = "관심 분야는 frontend, backend, data, ai, security 중 하나여야 합니다")
        String interest,

        @Schema(description = "멘토 여부", example = "true")
        @NotNull(message = "멘토 여부는 필수입니다")
        Boolean mentor,

        @Schema(description = "간단한 자기소개", example = "안녕하세요. 백엔드 개발에 관심이 많은 학생입니다.")
        String introduction,

        @Schema(description = "상세 자기소개", example = "저는 Spring Boot를 활용한 백엔드 개발에 관심이 많으며, 현재 여러 프로젝트를 진행하고 있습니다.")
        String detailIntroduction
) {
}
