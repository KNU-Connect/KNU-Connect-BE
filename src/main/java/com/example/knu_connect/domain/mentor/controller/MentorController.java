package com.example.knu_connect.domain.mentor.controller;

import com.example.knu_connect.domain.mentor.service.MentorService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "03. Mentor", description = "멘토 API")
@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;

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
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        MentorListResponseDto response = mentorService.getMentorList(career, interest, keyword, pageable);

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
        MentorDetailResponseDto response = mentorService.getMentorDetail(userId);

        return ResponseEntity.ok(response);
    }
}
