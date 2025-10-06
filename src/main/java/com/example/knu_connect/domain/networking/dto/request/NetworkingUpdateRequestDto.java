package com.example.knu_connect.domain.networking.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "네트워킹 수정 요청")
public record NetworkingUpdateRequestDto(
        @Schema(description = "제목", example = "백엔드 개발자 스터디 모집")
        @NotBlank(message = "제목은 필수입니다")
        String title,

        @Schema(description = "내용", example = "Spring Boot를 활용한 백엔드 개발 스터디원을 모집합니다.")
        @NotBlank(message = "내용은 필수입니다")
        String contents,

        @Schema(description = "최대 인원", example = "5")
        @NotNull(message = "최대 인원은 필수입니다")
        @Min(value = 2, message = "최대 인원은 2명 이상이어야 합니다")
        Integer maxNumber,

        @Schema(description = "대표자 ID", example = "1")
        @NotNull(message = "대표자 ID는 필수입니다")
        Long representativeId
) {
}
