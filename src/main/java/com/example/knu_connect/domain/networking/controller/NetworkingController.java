package com.example.knu_connect.domain.networking.controller;

import com.example.knu_connect.domain.networking.dto.request.NetworkingCreateRequestDto;
import com.example.knu_connect.domain.networking.dto.request.NetworkingUpdateRequestDto;
import com.example.knu_connect.domain.networking.dto.response.MyNetworkingListResponseDto;
import com.example.knu_connect.domain.networking.dto.response.NetworkingDetailResponseDto;
import com.example.knu_connect.domain.networking.dto.response.NetworkingListResponseDto;
import com.example.knu_connect.domain.networking.dto.response.ParticipantsResponseDto;
import com.example.knu_connect.domain.networking.service.NetworkingService;
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.global.annotation.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "02. Networking", description = "네트워킹 API")
@RestController
@RequestMapping("/api/networking")
@RequiredArgsConstructor
public class NetworkingController {

    private final NetworkingService networkingService;

    @Operation(
            summary = "네트워킹 생성",
            description = "새로운 네트워킹 게시물을 생성합니다"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "네트워킹 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
    })
    @Parameter(description = "채팅방 ID (채팅방에서 네트워킹 생성 시)", example = "1")
    @PostMapping
    public ResponseEntity<Void> createNetworking(
            @RequestParam(name = "chat_room_id") Long chatRoomId,
            @Valid @RequestBody NetworkingCreateRequestDto request,
            @AuthUser User user
    ) {
        networkingService.createNetworking(user, request, chatRoomId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
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
    @Parameters({
            @Parameter(description = "검색 키워드", example = "백엔드"),
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(description = "페이지 크기", example = "10")
    })
    @GetMapping
    public ResponseEntity<NetworkingListResponseDto> getNetworkingList(
            @RequestParam(required = false) String keyword,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @AuthUser User user
    ) {
        NetworkingListResponseDto response = networkingService.getNetworkingList(user, keyword, pageable);

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
    @Parameter(description = "네트워킹 ID", example = "1")
    @GetMapping("/{networking_id}")
    public ResponseEntity<NetworkingDetailResponseDto> getNetworkingDetail(
            @PathVariable("networking_id") Long networkingId,
            @AuthUser User user
    ) {
        NetworkingDetailResponseDto response = networkingService.getNetworkingDetail(user, networkingId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "게시물 수정",
            description = "네트워킹 게시물을 수정합니다"
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
            @Valid @RequestBody NetworkingUpdateRequestDto request,
            @AuthUser User user
    ) {
        networkingService.updateNetworking(user, request, networkingId);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "내가 작성한 글 보기",
            description = "현재 로그인한 사용자가 대표자로 있는 네트워킹 게시물 목록을 조회합니다"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MyNetworkingListResponseDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
    })
    @Parameters({
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(description = "페이지 크기", example = "10")
    })
    @GetMapping("/me")
    public ResponseEntity<MyNetworkingListResponseDto> getMyNetworkingList(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @AuthUser User user
    ) {
        MyNetworkingListResponseDto response = networkingService.getMyNetworkingList(user, pageable);

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
    @Parameter(description = "네트워킹 ID", example = "1")
    @GetMapping("/{networking_id}/participants")
    public ResponseEntity<ParticipantsResponseDto> getParticipants(
            @PathVariable("networking_id") Long networkingId,
            @AuthUser User user
    ) {
        ParticipantsResponseDto response = networkingService.getNetworkingParticipants(user, networkingId);
        return ResponseEntity.ok(response);
    }
}
