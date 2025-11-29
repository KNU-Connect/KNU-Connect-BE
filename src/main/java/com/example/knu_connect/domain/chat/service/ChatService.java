package com.example.knu_connect.domain.chat.service;

import com.example.knu_connect.domain.chat.dto.request.ChatMessageSendRequestDto;
import com.example.knu_connect.domain.chat.dto.request.ChatRoomCreateRequestDto;
import com.example.knu_connect.domain.chat.dto.response.*;

import java.util.List;

public interface ChatService {

    ChatRoomCreateResponseDto createChatRoom(Long userId, ChatRoomCreateRequestDto request);

    ChatMessageResponseDto sendMessage(Long userId, Long chatRoomId, ChatMessageSendRequestDto request);

    ChatMessageListResponseDto getChatMessageList(Long userId, Long chatRoomId, Long cursor, int size);

    ChatRoomListResponseDto getChatRoomList(Long userId);

    void deleteMessage(Long userId, Long chatRoomId, Long chatId);

    void leaveChatRoom(Long userId, Long chatRoomId);

    void openChatRoom(Long userId, Long chatRoomId);

    void closeChatRoom(Long userId, Long chatRoomId);

    void refreshChatRoom(Long userId, Long chatRoomId);

    List<ChatRoomParticipantResponseDto> getChatRoomParticipants(Long userId, Long chatRoomId);
}
