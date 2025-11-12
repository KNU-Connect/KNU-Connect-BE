package com.example.knu_connect.domain.chat.controller;

import com.example.knu_connect.domain.chat.dto.request.ChatMessageSendRequestDto;
import com.example.knu_connect.domain.chat.dto.response.ChatMessageResponseDto;
import com.example.knu_connect.domain.chat.service.ChatService;
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.global.annotation.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 메시지 전송
     * SEND /app/chat-rooms/{chat_room_id}/chats
     */
    @MessageMapping("/chat-rooms/{chatRoomId}/chats")
    public void sendMessage(
            @DestinationVariable Long chatRoomId,
            @Payload ChatMessageSendRequestDto request,
            @AuthUser User user
    ) {
        log.info("메시지 전송 요청 - 채팅방 ID: {}, 사용자 ID: {}", chatRoomId, user.getId());

        ChatMessageResponseDto response = chatService.sendMessage(user.getId(), chatRoomId, request);
        
    }

    /**
     * 채팅방 참여 (열기)
     * SEND /app/chat-rooms/{chat_room_id}/open
     */
    @MessageMapping("/chat-rooms/{chatRoomId}/open")
    public void openChatRoom(
            @DestinationVariable Long chatRoomId,
            @AuthUser User user
    ) {
        log.info("채팅방 열기 요청 - 채팅방 ID: {}, 사용자 ID: {}", chatRoomId, user.getId());
        chatService.openChatRoom(user.getId(), chatRoomId);
    }

    /**
     * 채팅방 종료
     * SEND /app/chat-rooms/{chat_room_id}/close
     */
    @MessageMapping("/chat-rooms/{chatRoomId}/close")
    public void closeChatRoom(
            @DestinationVariable Long chatRoomId,
            @AuthUser User user
    ) {
        log.info("채팅방 닫기 요청 - 채팅방 ID: {}, 사용자 ID: {}", chatRoomId, user.getId());
        chatService.closeChatRoom(user.getId(), chatRoomId);
    }

    /**
     * 채팅방 활동 갱신
     * SEND /app/chat-rooms/{chat_room_id}/refresh
     */
    @MessageMapping("/chat-rooms/{chatRoomId}/refresh")
    public void refreshChatRoom(
            @DestinationVariable Long chatRoomId,
            @AuthUser User user
    ) {
        log.debug("채팅방 활동 갱신 - 채팅방 ID: {}, 사용자 ID: {}", chatRoomId, user.getId());
        chatService.refreshChatRoom(user.getId(), chatRoomId);
    }
}
