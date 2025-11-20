package com.example.knu_connect.domain.user.controller;

import com.example.knu_connect.domain.user.dto.request.UserUpdateRequestDto;
import com.example.knu_connect.domain.user.dto.response.UserInfoResponseDto;
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.domain.user.service.UserService;
import com.example.knu_connect.global.annotation.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    private final UserService userService;

    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 사용자의 정보를 조회합니다"
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
    public ResponseEntity<UserInfoResponseDto> getUserInfo(@AuthUser User user) {
        UserInfoResponseDto response = userService.getUserInfo(user.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "유저 정보 수정",
            description = "현재 로그인한 사용자의 정보를 수정합니다"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
    })
    @PatchMapping
    public ResponseEntity<Void> updateUserInfo(@Valid @RequestBody UserUpdateRequestDto request,
                                               @AuthUser User user) {
        userService.updateUserInfo(user.getId(), request);
        return ResponseEntity.ok().build();
    }
}
