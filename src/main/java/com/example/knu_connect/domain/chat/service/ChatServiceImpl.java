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
        log.debug("Fetching chat room list for user {}", userId);
        
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByUserId(userId);
        log.debug("Found {} chat rooms for user {}", chatRooms.size(), userId);
        
        // 채팅방이 없으면 빈 리스트 반환
        if (chatRooms.isEmpty()) {
            return new ChatRoomListResponseDto(List.of());
        }

        // 채팅방 ID 목록 추출
        List<Long> chatRoomIds = chatRooms.stream()
                .map(ChatRoom::getId)
                .collect(Collectors.toList());
        
        log.debug("Chat room IDs: {}", chatRoomIds);

        // 참여자 정보를 User와 함께 조회
        List<ChatRoom> chatRoomsWithUsers = chatRoomRepository.findAllWithParticipantsAndUsers(chatRoomIds);
        log.debug("Loaded {} chat rooms with participants and users", chatRoomsWithUsers.size());

        List<ChatRoomListResponseDto.ChatRoomInfo> chatRoomInfos = chatRoomsWithUsers.stream()
                .map(chatRoom -> {
                    log.debug("Processing chat room ID: {}, participants count: {}", 
                            chatRoom.getId(), chatRoom.getParticipants().size());
                    
                    // 상대방 이름들을 조회하여 ", "로 연결
                    String title = chatRoom.getParticipants().stream()
                            .filter(p -> {
                                boolean isNotMe = !p.getUserId().equals(userId);
                                log.debug("Participant user ID: {}, is not me: {}, user name: {}", 
                                        p.getUserId(), isNotMe, p.getUser() != null ? p.getUser().getName() : "null");
                                return isNotMe;
                            })
                            .map(p -> p.getUser().getName())
                            .collect(Collectors.joining(", "));
                    
                    log.debug("Chat room {} title: {}", chatRoom.getId(), title);
                    
                    // 이름이 없으면 기본값
                    if (title.isEmpty() || title.isBlank()) {
                        title = "알 수 없음";
                    }

                    // 최근 메시지 가져오기
                    ChatMessage recentMessage = chatMessageRepository
                            .findFirstByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId())
                            .orElse(null);

                    // 안읽은 메시지 개수 계산
                    Long unreadCount = calculateUnreadCount(userId, chatRoom.getId());

                    return new ChatRoomListResponseDto.ChatRoomInfo(
                            chatRoom.getId(),
                            title,
                            unreadCount,
                            recentMessage != null ? recentMessage.getContents() : "",
                            recentMessage != null ? recentMessage.getCreatedAt() : chatRoom.getCreatedAt()
                    );
                })
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

        // 사용자가 채팅방 참여자인지 확인
        if (!chatRoom.hasParticipant(userId)) {
            throw new BusinessException(ErrorCode.CHAT_PARTICIPANTS_NOT_FOUND);
        }

        // 참여자 삭제
        chatParticipantsRepository.deleteByUserIdAndChatRoomId(userId, chatRoomId);

        networkingRepository.findByChatRoomId(chatRoomId)
                .ifPresent(Networking::leave);

        // 참여자가 0명이면 채팅방 삭제
        Long participantCount = chatParticipantsRepository.countByChatRoomId(chatRoomId);

        if (participantCount == 0) {
            networkingRepository.findByChatRoomId(chatRoomId)
                    .ifPresent(networkingRepository::delete);

            // 채팅방 삭제
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
                            user.getDepartment(),
                            user.getCareer(),
                            user.getInterest(),
                            user.getMbti(),
                            user.getIntroduction()
                    );
                })
                .collect(Collectors.toList());
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
