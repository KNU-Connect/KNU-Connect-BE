package com.example.knu_connect.domain.user.controller;

import com.example.knu_connect.domain.user.dto.request.UserUpdateRequestDto;
import com.example.knu_connect.domain.user.dto.response.UserInfoResponseDto;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "04. User", description = "사용자 정보 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 사용자의 정보를 조회합니다",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserInfoResponseDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
    })
    @GetMapping
    public ResponseEntity<UserInfoResponseDto> getUserInfo() {
        // TODO: 내 정보 조회 로직 구현
        UserInfoResponseDto response = new UserInfoResponseDto(
                "홍길동",
                "student",
                "computer",
                "employment",
                "ENFP",
                "backend",
                true,
                "안녕하세요. 백엔드 개발에 관심이 많은 학생입니다.",
                "저는 Spring Boot를 활용한 백엔드 개발에 관심이 많으며, 현재 여러 프로젝트를 진행하고 있습니다."
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "유저 정보 수정",
            description = "현재 로그인한 사용자의 정보를 수정합니다",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
    })
    @PutMapping
    public ResponseEntity<Void> updateUserInfo(@Valid @RequestBody UserUpdateRequestDto request) {
        // TODO: 유저 정보 수정 로직 구현
        return ResponseEntity.ok().build();
    }
}
