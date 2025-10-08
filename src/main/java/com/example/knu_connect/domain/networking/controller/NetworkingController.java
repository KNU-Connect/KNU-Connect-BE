package com.example.knu_connect.domain.networking.controller;

import com.example.knu_connect.domain.networking.dto.request.NetworkingCreateRequestDto;
import com.example.knu_connect.domain.networking.dto.request.NetworkingUpdateRequestDto;
import com.example.knu_connect.domain.networking.dto.response.MyNetworkingListResponseDto;
import com.example.knu_connect.domain.networking.dto.response.NetworkingDetailResponseDto;
import com.example.knu_connect.domain.networking.dto.response.NetworkingListResponseDto;
import com.example.knu_connect.domain.networking.dto.response.ParticipantsResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;

@Tag(name = "02. Networking", description = "네트워킹 API")
@RestController
@RequestMapping("/api/networking")
@RequiredArgsConstructor
public class NetworkingController {

    @Operation(
            summary = "네트워킹 생성",
            description = "새로운 네트워킹 게시물을 생성합니다",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "네트워킹 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Void> createNetworking(
            @Parameter(description = "채팅방 ID", example = "1")
            @RequestParam(name = "chat_room_id") Long chatRoomId,
            @Valid @RequestBody NetworkingCreateRequestDto request
    ) {
        // TODO: 네트워킹 생성 로직 구현
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "게시물 목록 조회",
            description = "네트워킹 게시물 목록을 조회합니다. 키워드로 검색할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = NetworkingListResponseDto.class))
            )
    })
    @GetMapping
    public ResponseEntity<NetworkingListResponseDto> getNetworkingList(
            @Parameter(description = "검색 키워드", example = "백엔드")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") Integer size
    ) {
        // TODO: 게시물 목록 조회 로직 구현
        NetworkingListResponseDto response = new NetworkingListResponseDto(
                Collections.emptyList(),
                page,
                size,
                false
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "게시물 상세 정보 조회",
            description = "특정 네트워킹 게시물의 상세 정보를 조회합니다"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = NetworkingDetailResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없음", content = @Content)
    })
    @GetMapping("/{networking_id}")
    public ResponseEntity<NetworkingDetailResponseDto> getNetworkingDetail(
            @Parameter(description = "네트워킹 ID", example = "1")
            @PathVariable("networking_id") Long networkingId
    ) {
        // TODO: 게시물 상세 정보 조회 로직 구현
        NetworkingDetailResponseDto.RepresentativeDto representative =
                new NetworkingDetailResponseDto.RepresentativeDto(
                        "홍길동",
                        "student",
                        "computer",
                        "employment",
                        "ENFP",
                        "backend",
                        "안녕하세요."
                );

        NetworkingDetailResponseDto response = new NetworkingDetailResponseDto(
                1L,
                "샘플 제목",
                "샘플 내용",
                3,
                5,
                LocalDateTime.now(),
                representative
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "게시물 수정",
            description = "네트워킹 게시물을 수정합니다",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없음", content = @Content)
    })
    @PutMapping("/{networking_id}")
    public ResponseEntity<Void> updateNetworking(
            @Parameter(description = "네트워킹 ID", example = "1")
            @PathVariable("networking_id") Long networkingId,
            @Valid @RequestBody NetworkingUpdateRequestDto request
    ) {
        // TODO: 게시물 수정 로직 구현
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "내가 작성한 글 보기",
            description = "현재 로그인한 사용자가 작성한 네트워킹 게시물 목록을 조회합니다",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MyNetworkingListResponseDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<MyNetworkingListResponseDto> getMyNetworkingList(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") Integer size
    ) {
        // TODO: 내가 작성한 글 조회 로직 구현
        MyNetworkingListResponseDto response = new MyNetworkingListResponseDto(
                Collections.emptyList(),
                page,
                size,
                false
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "네트워킹 참여인원 목록 조회",
            description = "특정 네트워킹 게시물의 참여자 목록을 조회합니다"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ParticipantsResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없음", content = @Content)
    })
    @GetMapping("/{networking_id}/participants")
    public ResponseEntity<ParticipantsResponseDto> getParticipants(
            @Parameter(description = "네트워킹 ID", example = "1")
            @PathVariable("networking_id") Long networkingId
    ) {
        // TODO: 참여인원 목록 조회 로직 구현
        ParticipantsResponseDto response = new ParticipantsResponseDto(
                Collections.emptyList()
        );
        return ResponseEntity.ok(response);
    }
}
