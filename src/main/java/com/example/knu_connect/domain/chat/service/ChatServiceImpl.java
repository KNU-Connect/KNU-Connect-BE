package com.example.knu_connect.domain.chat.service;

import com.example.knu_connect.domain.chat.entitiy.ChatMessage;
import com.example.knu_connect.domain.chat.entitiy.ChatParticipants;
import com.example.knu_connect.domain.chat.entitiy.ChatRoom;
import com.example.knu_connect.domain.chat.dto.request.ChatMessageSendRequestDto;
import com.example.knu_connect.domain.chat.dto.request.ChatRoomCreateRequestDto;
import com.example.knu_connect.domain.chat.dto.response.*;
import com.example.knu_connect.domain.chat.repository.ChatMessageRepository;
import com.example.knu_connect.domain.chat.repository.ChatParticipantsRepository;
import com.example.knu_connect.domain.chat.repository.ChatRoomRepository;
import com.example.knu_connect.domain.networking.entitiy.Networking;
import com.example.knu_connect.domain.networking.repository.NetworkingRepository;
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.domain.user.repository.UserRepository;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantsRepository chatParticipantsRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisChatManager redisChatManager;
    private final NetworkingRepository networkingRepository;

    // 멘토 찾기에서 대화 시작 시 1대1 채팅방 생성
    @Override
    @Transactional
    public ChatRoomCreateResponseDto createChatRoom(Long userId, ChatRoomCreateRequestDto request) {
        Long participantId = request.participantId();

        // 자기 자신과 채팅방 생성 방지
        if (userId.equals(participantId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 참여자 존재 확인
        User participant = userRepository.findById(participantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 기존 채팅방 확인
        return chatRoomRepository.findByTwoParticipants(userId, participantId)
                .map(chatRoom -> new ChatRoomCreateResponseDto(chatRoom.getId()))
                .orElseGet(() -> {
                    // 새 채팅방 생성
                    ChatRoom newChatRoom = ChatRoom.create();
                    chatRoomRepository.save(newChatRoom);

                    // 참여자 추가 (lastReadMessageId = 0으로 초기화)
                    ChatParticipants participant1 = ChatParticipants.builder()
                            .user(currentUser)
                            .chatRoom(newChatRoom)
                            .lastReadMessageId(0L)
                            .build();
                    ChatParticipants participant2 = ChatParticipants.builder()
                            .user(participant)
                            .chatRoom(newChatRoom)
                            .lastReadMessageId(0L)
                            .build();

                    chatParticipantsRepository.save(participant1);
                    chatParticipantsRepository.save(participant2);

                    return new ChatRoomCreateResponseDto(newChatRoom.getId());
                });
    }

    @Override
    @Transactional
    public ChatMessageResponseDto sendMessage(Long userId, Long chatRoomId, ChatMessageSendRequestDto request) {
        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 사용자가 채팅방 참여자인지 확인
        if (!chatRoom.hasParticipant(userId)) {
            throw new BusinessException(ErrorCode.CHAT_PARTICIPANTS_NOT_FOUND);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 메시지 저장
        ChatMessage message = ChatMessage.builder()
                .user(user)
                .chatRoom(chatRoom)
                .contents(request.content())
                .build();

        chatMessageRepository.save(message);

        ChatMessageResponseDto response = ChatMessageResponseDto.from(message);

        // WebSocket으로 메시지 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chat-rooms/" + chatRoomId, response);

        // 메시지를 보낸 사용자가 채팅방을 열고 있다면 lastReadMessageId 업데이트
        if (redisChatManager.isUserActive(chatRoomId, userId)) {
            updateLastReadMessageId(userId, chatRoomId, message.getId());
        }

        // 상대방이 채팅방을 열고 있지 않으면 읽지 않은 메세지 수 전송
        notifyUnreadCountToInactiveUsers(chatRoom, userId);

        return response;
    }

    @Override
    public ChatMessageListResponseDto getChatMessageList(Long userId, Long chatRoomId, Long cursor, int size) {
        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 사용자가 채팅방 참여자인지 확인
        if (!chatRoom.hasParticipant(userId)) {
            throw new BusinessException(ErrorCode.CHAT_PARTICIPANTS_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(0, size);
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdWithCursor(
                chatRoomId, cursor, pageable);

        // 메시지가 없으면 빈 리스트 반환
        List<ChatMessageResponseDto> messageDtos = messages.stream()
                .map(ChatMessageResponseDto::from)
                .collect(Collectors.toList());

        // 다음 페이지 존재 여부 (요청한 size만큼 가져왔으면 다음 페이지 있음)
        boolean hasNext = messages.size() >= size;
        Long nextCursor = hasNext && !messages.isEmpty()
                ? messages.get(messages.size() - 1).getId() 
                : null;

        log.debug("Fetched {} messages for chat room {}, hasNext: {}, nextCursor: {}", 
                messages.size(), chatRoomId, hasNext, nextCursor);

        return new ChatMessageListResponseDto(
                userId,
                messageDtos,
                hasNext,
                nextCursor
        );
    }

    @Override
    public ChatRoomListResponseDto getChatRoomList(Long userId) {

        List<ChatRoom> chatRooms = chatRoomRepository.findAllByUserId(userId);

        if (chatRooms.isEmpty()) {
            return new ChatRoomListResponseDto(List.of());
        }

        List<Long> chatRoomIds = chatRooms.stream()
                .map(ChatRoom::getId)
                .collect(Collectors.toList());

        List<ChatRoom> chatRoomsWithUsers = chatRoomRepository.findAllWithParticipantsAndUsers(chatRoomIds);

        List<ChatRoomListResponseDto.ChatRoomInfo> chatRoomInfos = chatRoomsWithUsers.stream()
                .map(chatRoom -> {
                    String title;

                    // 네트워킹 채팅방인지 확인
                    Networking networking = networkingRepository.findByChatRoomId(chatRoom.getId())
                            .orElse(null);

                    if (networking != null) {
                        // 네트워킹 채팅방인 경우: 네트워킹 제목 사용
                        title = networking.getTitle();
                    } else {
                        // 멘토/일반 1:1 채팅방인 경우: 상대방 이름 사용
                        title = chatRoom.getParticipants().stream()
                                .filter(p -> !p.getUserId().equals(userId))
                                .map(p -> p.getUser().getName())
                                .collect(Collectors.joining(", "));

                        if (title.isEmpty() || title.isBlank()) {
                            title = "알 수 없음";
                        }
                    }

                    ChatMessage recentMessage = chatMessageRepository
                            .findFirstByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId())
                            .orElse(null);

                    Long unreadCount = calculateUnreadCount(userId, chatRoom.getId());

                    return new ChatRoomListResponseDto.ChatRoomInfo(
                            chatRoom.getId(),
                            title,
                            unreadCount,
                            recentMessage != null ? recentMessage.getContents() : "",
                            recentMessage != null ? recentMessage.getCreatedAt() : chatRoom.getCreatedAt()
                    );
                })
                .sorted(Comparator.comparing(ChatRoomListResponseDto.ChatRoomInfo::recentDate).reversed())
                .collect(Collectors.toList());

        return new ChatRoomListResponseDto(chatRoomInfos);
    }

    @Override
    @Transactional
    public void deleteMessage(Long userId, Long chatRoomId, Long chatId) {
        ChatMessage message = chatMessageRepository.findById(chatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        // 본인이 작성한 메시지인지 확인
        if (!message.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_DELETE_MESSAGE);
        }

        // 메시지 삭제
        chatMessageRepository.delete(message);

        // WebSocket으로 삭제 알림
        MessageDeletedResponseDto response = new MessageDeletedResponseDto(chatId);
        messagingTemplate.convertAndSend("/topic/chat-rooms/" + chatRoomId + "/updates", response);
    }

    @Override
    @Transactional
    public void leaveChatRoom(Long userId, Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        ChatParticipants participantToRemove = chatRoom.getParticipants().stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_PARTICIPANTS_NOT_FOUND));

        chatRoom.getParticipants().remove(participantToRemove);

        networkingRepository.findByChatRoomId(chatRoomId)
                .ifPresent(Networking::leave);

        if (chatRoom.getParticipants().isEmpty()) {
            networkingRepository.findByChatRoomId(chatRoomId)
                    .ifPresent(networkingRepository::delete);

            chatRoomRepository.delete(chatRoom);
        }
    }

    @Override
    @Transactional
    public void openChatRoom(Long userId, Long chatRoomId) {
        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 사용자가 채팅방 참여자인지 확인
        if (!chatRoom.hasParticipant(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Redis에 활성화 표시
        redisChatManager.markUserActive(chatRoomId, userId);

        // 가장 최근 메시지 ID로 lastReadMessageId 업데이트
        Long latestMessageId = chatMessageRepository.findLatestMessageId(chatRoomId).orElse(0L);
        updateLastReadMessageId(userId, chatRoomId, latestMessageId);

        log.info("User {} opened chat room {}", userId, chatRoomId);
    }

    @Override
    @Transactional
    public void closeChatRoom(Long userId, Long chatRoomId) {
        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 사용자가 채팅방 참여자인지 확인
        if (!chatRoom.hasParticipant(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Redis에서 활성화 제거
        redisChatManager.markUserInactive(chatRoomId, userId);

        log.info("User {} closed chat room {}", userId, chatRoomId);
    }

    @Transactional
    @Override
    public void refreshChatRoom(Long userId, Long chatRoomId) {
        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 사용자가 채팅방 참여자인지 확인
        if (!chatRoom.hasParticipant(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Redis TTL 갱신
        redisChatManager.refreshUserActivity(chatRoomId, userId);

        log.debug("User {} refreshed chat room {}", userId, chatRoomId);
    }

    @Override
    public List<ChatRoomParticipantResponseDto> getChatRoomParticipants(Long userId, Long chatRoomId) {

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        if (!chatRoom.hasParticipant(userId)) {
            throw new BusinessException(ErrorCode.CHAT_PARTICIPANTS_NOT_FOUND);
        }

        return chatRoom.getParticipants().stream()
                .map(participant -> {
                    User user = participant.getUser();

                    return new ChatRoomParticipantResponseDto(
                            user.getId(),
                            user.getName(),
                            user.getStatus(),
                            user.getDepartment(),
                            user.getCareer(),
                            user.getInterest(),
                            user.getMbti(),
                            user.getIntroduction()
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public ChatRoomTypeResponseDto getChatRoomType(Long userId, Long chatRoomId) {

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        if (!chatRoom.hasParticipant(userId)) {
            throw new BusinessException(ErrorCode.CHAT_PARTICIPANTS_NOT_FOUND);
        }

        return networkingRepository.findByChatRoomId(chatRoomId)
                .map(networking -> new ChatRoomTypeResponseDto(
                        true,
                        networking.getId(),
                        networking.getTitle()
                ))
                .orElseGet(() -> new ChatRoomTypeResponseDto(
                        false,
                        null,
                        null
                ));
    }

    // ========== Private Helper Methods ==========

    // 안읽은 메시지 수 계산
    private Long calculateUnreadCount(Long userId, Long chatRoomId) {
        ChatParticipants participant = chatParticipantsRepository
                .findByUserIdAndChatRoomId(userId, chatRoomId)
                .orElse(null);

        if (participant == null) {
            return 0L;
        }

        Long lastReadMessageId = participant.getLastReadMessageId();
        return chatMessageRepository.countUnreadMessages(chatRoomId, lastReadMessageId, userId);
    }


    // lastReadMessageId 업데이트
    @Transactional
    public void updateLastReadMessageId(Long userId, Long chatRoomId, Long messageId) {
        ChatParticipants participant = chatParticipantsRepository
                .findByUserIdAndChatRoomId(userId, chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        participant.updateLastReadMessageId(messageId);
        chatParticipantsRepository.save(participant);

        log.debug("Updated lastReadMessageId for user {} in chat room {} to {}", userId, chatRoomId, messageId);
    }


    // 비활성 사용자들에게 안읽은 메시지 수 알림
    private void notifyUnreadCountToInactiveUsers(ChatRoom chatRoom, Long senderUserId) {
        chatRoom.getParticipants().stream()
                .filter(participant -> !participant.getUserId().equals(senderUserId))
                .filter(participant -> !redisChatManager.isUserActive(chatRoom.getId(), participant.getUserId()))
                .forEach(participant -> {
                    Long unreadCount = calculateUnreadCount(participant.getUserId(), chatRoom.getId());
                    UnreadCountNotificationDto notification = new UnreadCountNotificationDto(
                            chatRoom.getId(),
                            unreadCount
                    );

                    messagingTemplate.convertAndSendToUser(
                            participant.getUserId().toString(),
                            "/queue/chat-rooms/" + chatRoom.getId() + "/unread",
                            notification
                    );

                    log.debug("Sent unread count notification to user {}: {}", participant.getUserId(), unreadCount);
                });
    }
}
