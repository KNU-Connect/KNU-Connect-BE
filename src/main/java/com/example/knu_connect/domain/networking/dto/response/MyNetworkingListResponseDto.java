package com.example.knu_connect.domain.networking.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "내가 작성한 네트워킹 목록 조회 응답")
public record MyNetworkingListResponseDto(
        @Schema(description = "게시물 목록")
        List<MyNetworkingBoardDto> boards,

        @Schema(description = "페이지 번호", example = "0")
        Integer page,

        @Schema(description = "페이지 크기", example = "10")
        Integer size,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        Boolean hasNext
) {
    @Schema(description = "내가 작성한 네트워킹 게시물 정보")
    public record MyNetworkingBoardDto(
            @Schema(description = "게시물 ID", example = "1")
            Long id,

            @Schema(description = "제목", example = "백엔드 개발자 스터디 모집")
            String title,

            @Schema(description = "작성자", example = "홍길동")
            String writer,

            @Schema(description = "내용", example = "Spring Boot를 활용한 백엔드 개발 스터디원을 모집합니다.")
            String contents,

            @Schema(description = "현재 인원", example = "3")
            Integer curNumber,

            @Schema(description = "최대 인원", example = "5")
            Integer maxNumber,

            @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
            LocalDateTime createdAt
    ) {
    }
}
