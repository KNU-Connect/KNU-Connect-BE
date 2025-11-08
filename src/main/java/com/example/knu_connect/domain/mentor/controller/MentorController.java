package com.example.knu_connect.domain.mentor.controller;

import com.example.knu_connect.domain.mentor.dto.response.MentorDetailResponseDto;
import com.example.knu_connect.domain.mentor.dto.response.MentorListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Tag(name = "03. Mentor", description = "멘토 API")
@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
public class MentorController {

    @Operation(
            summary = "멘토 목록 조회",
            description = "멘토 목록을 조회합니다. 진로, 관심 분야, 키워드로 필터링할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MentorListResponseDto.class))
            )
    })
    @Parameters({
            @Parameter(description = "진로 필터", example = "employment", schema = @Schema(allowableValues = {"employment", "startup", "matriculation"})),
            @Parameter(description = "관심 분야 필터", example = "backend", schema = @Schema(allowableValues = {"frontend", "backend", "data", "ai", "security"})),
            @Parameter(description = "검색 키워드", example = "백엔드"),
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(description = "페이지 크기", example = "10")
    })
    @GetMapping
    public ResponseEntity<MentorListResponseDto> getMentorList(
            @RequestParam(required = false) String career,
            @RequestParam(required = false) String interest,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        // TODO: 멘토 목록 조회 로직 구현
        MentorListResponseDto response = new MentorListResponseDto(
                Collections.emptyList(),
                page,
                size,
                false
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "멘토 상세 조회",
            description = "특정 멘토의 상세 정보를 조회합니다"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MentorDetailResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "멘토를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/{user_id}")
    public ResponseEntity<MentorDetailResponseDto> getMentorDetail(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable("user_id") Long userId
    ) {
        // TODO: 멘토 상세 조회 로직 구현
        MentorDetailResponseDto response = new MentorDetailResponseDto(
                "홍길동",
                "computer",
                "graduate",
                "employment",
                "backend",
                "ENFP",
                "안녕하세요. 백엔드 개발에 관심이 많은 멘토입니다.",
                "저는 5년간 백엔드 개발을 해왔으며, Spring Boot와 JPA를 주로 사용합니다."
        );
        return ResponseEntity.ok(response);
    }
}
