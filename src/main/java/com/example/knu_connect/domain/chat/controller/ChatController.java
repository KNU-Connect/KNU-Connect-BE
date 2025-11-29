package com.example.knu_connect.domain.chat.controller;

import com.example.knu_connect.domain.chat.dto.request.ChatRoomCreateRequestDto;
import com.example.knu_connect.domain.chat.dto.response.ChatMessageListResponseDto;
import com.example.knu_connect.domain.chat.dto.response.ChatRoomCreateResponseDto;
import com.example.knu_connect.domain.chat.dto.response.ChatRoomListResponseDto;
import com.example.knu_connect.domain.chat.dto.response.ChatRoomParticipantResponseDto;
import com.example.knu_connect.domain.chat.service.ChatService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "04. Chat", description = "채팅 API")
@RestController
@RequestMapping("/api/chat-rooms")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(
            summary = "채팅방 생성",
            description = "새로운 채팅방을 생성하거나 기존 채팅방을 반환합니다"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "채팅방 생성 성공",
                    content = @Content(schema = @Schema(implementation = ChatRoomCreateResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "404", description = "참여자를 찾을 수 없음", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ChatRoomCreateResponseDto> createChatRoom(
            @AuthUser User user,
            @Valid @RequestBody ChatRoomCreateRequestDto request
    ) {
        ChatRoomCreateResponseDto response = chatService.createChatRoom(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "채팅방 목록 조회",
            description = "현재 사용자가 참여 중인 채팅방 목록을 조회합니다"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ChatRoomListResponseDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
    })
    @GetMapping
    public ResponseEntity<ChatRoomListResponseDto> getChatRoomList(@AuthUser User user) {
        ChatRoomListResponseDto response = chatService.getChatRoomList(user.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "채팅 기록 조회",
            description = "커서 페이지네이션을 통해 채팅 기록을 조회합니다. (기본 20개씩 불러옴)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ChatMessageListResponseDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음", content = @Content)
    })
    @Parameters({
            @Parameter(name = "chat_room_id" , description = "채팅방 ID", example = "1"),
            @Parameter(name = "cursor", description = "마지막으로 받은 메시지 ID (null이면 최신부터)", example = "100"),
            @Parameter(name = "size", description = "조회할 메시지 수", example = "20")
    })
    @GetMapping("/{chat_room_id}")
    public ResponseEntity<ChatMessageListResponseDto> getChatMessageList(
            @AuthUser User user,
            @PathVariable("chat_room_id") Long chatRoomId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        ChatMessageListResponseDto response = chatService.getChatMessageList(user.getId(), chatRoomId, cursor, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "메시지 삭제",
            description = "특정 메시지를 삭제합니다"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "메시지를 찾을 수 없음", content = @Content)
    })
    @Parameters({
            @Parameter(name = "chat_room_id", description = "채팅방 ID", example = "1"),
            @Parameter(name = "chat_id", description = "메시지 ID", example = "1")
    })
    @DeleteMapping("/{chat_room_id}/chats/{chat_id}")
    public ResponseEntity<Void> deleteMessage(
            @AuthUser User user,
            @PathVariable("chat_room_id") Long chatRoomId,
            @PathVariable("chat_id") Long chatId
    ) {
        chatService.deleteMessage(user.getId(), chatRoomId, chatId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "채팅방 탈퇴",
            description = "채팅방에서 탈퇴합니다"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음", content = @Content)
    })
    @Parameter(name = "chat_room_id", description = "채팅방 ID", example = "1")
    @DeleteMapping("/{chat_room_id}")
    public ResponseEntity<Void> leaveChatRoom(
            @AuthUser User user,
            @PathVariable("chat_room_id") Long chatRoomId
    ) {
        chatService.leaveChatRoom(user.getId(), chatRoomId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "채팅방 참여자 목록 조회",
            description = "네트워킹 생성 시 대표자 선정을 위해 채팅방의 참여자 상세 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ChatRoomParticipantResponseDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음(채팅방 참여자가 아님)", content = @Content),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음", content = @Content)
    })
    @Parameter(name = "chat_room_id", description = "채팅방 ID", example = "1")
    @GetMapping("/{chat_room_id}/participants")
    public ResponseEntity<List<ChatRoomParticipantResponseDto>> getChatRoomParticipants(
            @AuthUser User user,
            @PathVariable("chat_room_id") Long chatRoomId
    ) {
        List<ChatRoomParticipantResponseDto> response = chatService.getChatRoomParticipants(user.getId(), chatRoomId);
        return ResponseEntity.ok(response);
    }
}
