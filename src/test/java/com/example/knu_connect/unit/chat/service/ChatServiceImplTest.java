package com.example.knu_connect.unit.chat.service;

import com.example.knu_connect.domain.chat.dto.request.ChatMessageSendRequestDto;
import com.example.knu_connect.domain.chat.dto.request.ChatRoomCreateRequestDto;
import com.example.knu_connect.domain.chat.dto.response.*;
import com.example.knu_connect.domain.chat.entitiy.ChatMessage;
import com.example.knu_connect.domain.chat.entitiy.ChatParticipants;
import com.example.knu_connect.domain.chat.entitiy.ChatRoom;
import com.example.knu_connect.domain.chat.repository.ChatMessageRepository;
import com.example.knu_connect.domain.chat.repository.ChatParticipantsRepository;
import com.example.knu_connect.domain.chat.repository.ChatRoomRepository;
import com.example.knu_connect.domain.chat.service.ChatServiceImpl;
import com.example.knu_connect.domain.chat.service.RedisChatManager;
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.domain.user.repository.UserRepository;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @InjectMocks
    private ChatServiceImpl chatService;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatParticipantsRepository chatParticipantsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private RedisChatManager redisChatManager;

    private User user1;
    private User user2;
    private ChatRoom chatRoom;
    private ChatParticipants participant1;
    private ChatParticipants participant2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .name("홍길동")
                .email("hong@test.com")
                .password("password")
                .build();
        setId(user1, 1L);

        user2 = User.builder()
                .name("김철수")
                .email("kim@test.com")
                .password("password")
                .build();
        setId(user2, 2L);

        chatRoom = ChatRoom.create();
        setId(chatRoom, 1L);

        participant1 = ChatParticipants.builder()
                .user(user1)
                .chatRoom(chatRoom)
                .lastReadMessageId(0L)
                .build();

        participant2 = ChatParticipants.builder()
                .user(user2)
                .chatRoom(chatRoom)
                .lastReadMessageId(0L)
                .build();

        chatRoom.addParticipant(participant1);
        chatRoom.addParticipant(participant2);
    }

    // Reflection을 사용하여 ID 설정
    private void setId(Object target, Long id) {
        try {
            var field = target.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            try {
                var field = target.getClass().getDeclaredField("id");
                field.setAccessible(true);
                field.set(target, id);
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Nested
    class 채팅방_생성_테스트 {

        @Test
        void 채팅방_생성() {
            // given
            Long userId = 1L;
            Long participantId = 2L;
            ChatRoomCreateRequestDto request = new ChatRoomCreateRequestDto(participantId);

            given(userRepository.findById(participantId)).willReturn(Optional.of(user2));
            given(userRepository.findById(userId)).willReturn(Optional.of(user1));
            given(chatRoomRepository.findByTwoParticipants(userId, participantId))
                    .willReturn(Optional.empty());
            
            // save 호출 시 전달받은 chatRoom에 ID를 설정하고 반환
            given(chatRoomRepository.save(any(ChatRoom.class))).willAnswer(invocation -> {
                ChatRoom savedRoom = invocation.getArgument(0);
                setId(savedRoom, 1L);
                return savedRoom;
            });

            // when
            ChatRoomCreateResponseDto response = chatService.createChatRoom(userId, request);

            // then
            assertThat(response.chatRoomId()).isEqualTo(1L);
            verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
            verify(chatParticipantsRepository, times(2)).save(any(ChatParticipants.class));
        }

        @Test
        void 이미_존재하는_채팅방이_존재하는_경우() {
            // given
            Long userId = 1L;
            Long participantId = 2L;
            ChatRoomCreateRequestDto request = new ChatRoomCreateRequestDto(participantId);

            given(userRepository.findById(userId)).willReturn(Optional.of(user1));
            given(userRepository.findById(participantId)).willReturn(Optional.of(user2));
            given(chatRoomRepository.findByTwoParticipants(userId, participantId))
                    .willReturn(Optional.of(chatRoom));

            // when
            ChatRoomCreateResponseDto response = chatService.createChatRoom(userId, request);

            // then
            assertThat(response.chatRoomId()).isEqualTo(1L);
            verify(chatRoomRepository, never()).save(any(ChatRoom.class));
        }

        @Test
        void 자기자신과_채팅방_생성시_예외() {
            // given
            Long userId = 1L;
            ChatRoomCreateRequestDto request = new ChatRoomCreateRequestDto(userId);

            // when & then
            assertThatThrownBy(() -> chatService.createChatRoom(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }

        @Test
        void 존재하지_않는_사용자와_채팅방_생성() {
            // given
            Long userId = 1L;
            Long participantId = 999L;
            ChatRoomCreateRequestDto request = new ChatRoomCreateRequestDto(participantId);

            given(userRepository.findById(participantId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> chatService.createChatRoom(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    class 메세지_전송_테스트 {

        @Test
        void 메세지_전송() {
            // given
            Long userId = 1L;
            Long chatRoomId = 1L;
            ChatMessageSendRequestDto request = new ChatMessageSendRequestDto("안녕하세요");

            given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
            given(userRepository.findById(userId)).willReturn(Optional.of(user1));
            given(redisChatManager.isUserActive(chatRoomId, userId)).willReturn(false);
            
            // save 호출 시 전달받은 message에 ID를 설정하고 반환
            given(chatMessageRepository.save(any(ChatMessage.class))).willAnswer(invocation -> {
                ChatMessage savedMessage = invocation.getArgument(0);
                setId(savedMessage, 1L);
                return savedMessage;
            });

            // when
            ChatMessageResponseDto response = chatService.sendMessage(userId, chatRoomId, request);

            // then
            assertThat(response.content()).isEqualTo("안녕하세요");
            assertThat(response.senderId()).isEqualTo(1L);
            verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
            verify(messagingTemplate, times(1))
                    .convertAndSend(eq("/topic/chat-rooms/" + chatRoomId), any(ChatMessageResponseDto.class));
        }

        @Test
        void 채팅방_참여자가_아닌경우_메세지_전송() {
            // given
            Long userId = 999L;
            Long chatRoomId = 1L;
            ChatMessageSendRequestDto request = new ChatMessageSendRequestDto("안녕하세요");

            given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));

            // when & then
            assertThatThrownBy(() -> chatService.sendMessage(userId, chatRoomId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHAT_PARTICIPANTS_NOT_FOUND);
        }

        @Test
        void 존재하지_않는_채팅방에_메세지_전송() {
            // given
            Long userId = 1L;
            Long chatRoomId = 999L;
            ChatMessageSendRequestDto request = new ChatMessageSendRequestDto("안녕하세요");

            given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> chatService.sendMessage(userId, chatRoomId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHAT_ROOM_NOT_FOUND);
        }
    }

    @Nested
    class 채팅_기록_조회_테스트 {

        @Test
        void 채팅_기록_조회() {
            // given
            Long userId = 1L;
            Long chatRoomId = 1L;
            Long cursor = null;
            int size = 20;

            ChatMessage message1 = ChatMessage.builder()
                    .user(user1)
                    .chatRoom(chatRoom)
                    .contents("메시지1")
                    .build();
            setId(message1, 1L);

            ChatMessage message2 = ChatMessage.builder()
                    .user(user2)
                    .chatRoom(chatRoom)
                    .contents("메시지2")
                    .build();
            setId(message2, 2L);

            List<ChatMessage> messages = Arrays.asList(message1, message2);

            given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
            given(chatMessageRepository.findByChatRoomIdWithCursor(
                    eq(chatRoomId), eq(cursor), any(Pageable.class)))
                    .willReturn(messages);

            // when
            ChatMessageListResponseDto response = chatService.getChatMessageList(
                    userId, chatRoomId, cursor, size);

            // then
            assertThat(response.messages()).hasSize(2);
            assertThat(response.userId()).isEqualTo(userId);
            assertThat(response.hasNext()).isFalse(); // 2개만 가져왔으므로 false
        }

        @Test
        void 다음_페이지가_있는경우_채팅조회() {
            // given
            Long userId = 1L;
            Long chatRoomId = 1L;
            Long cursor = null;
            int size = 2;

            ChatMessage message1 = ChatMessage.builder()
                    .user(user1)
                    .chatRoom(chatRoom)
                    .contents("메시지1")
                    .build();
            setId(message1, 1L);

            ChatMessage message2 = ChatMessage.builder()
                    .user(user2)
                    .chatRoom(chatRoom)
                    .contents("메시지2")
                    .build();
            setId(message2, 2L);

            List<ChatMessage> messages = Arrays.asList(message1, message2);

            given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
            given(chatMessageRepository.findByChatRoomIdWithCursor(
                    eq(chatRoomId), eq(cursor), any(Pageable.class)))
                    .willReturn(messages);

            // when
            ChatMessageListResponseDto response = chatService.getChatMessageList(
                    userId, chatRoomId, cursor, size);

            // then
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isEqualTo(2L);
        }
    }

    @Nested
    class 채팅방_목록_조회_테스트 {

        @Test
        void 채팅방_목록_조회() {
            // given
            Long userId = 1L;
            List<ChatRoom> chatRooms = List.of(chatRoom);
            List<Long> chatRoomIds = List.of(1L);

            given(chatRoomRepository.findAllByUserId(userId)).willReturn(chatRooms);
            given(chatRoomRepository.findAllWithParticipantsAndUsers(chatRoomIds))
                    .willReturn(chatRooms);
            given(chatMessageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(1L))
                    .willReturn(Optional.empty());
            given(chatParticipantsRepository.findByUserIdAndChatRoomId(userId, 1L))
                    .willReturn(Optional.of(participant1));
            given(chatMessageRepository.countUnreadMessages(anyLong(), anyLong(), anyLong()))
                    .willReturn(0L);

            // when
            ChatRoomListResponseDto response = chatService.getChatRoomList(userId);

            // then
            assertThat(response.chatRooms()).hasSize(1);
            assertThat(response.chatRooms().get(0).title()).isEqualTo("김철수");
        }

        @Test
        void 채팅방이_없는_경우() {
            // given
            Long userId = 1L;

            given(chatRoomRepository.findAllByUserId(userId)).willReturn(List.of());

            // when
            ChatRoomListResponseDto response = chatService.getChatRoomList(userId);

            // then
            assertThat(response.chatRooms()).isEmpty();
        }
    }

    @Nested
    class 메세지_삭제_테스트 {

        @Test
        void 메세지_삭제() {
            // given
            Long userId = 1L;
            Long chatRoomId = 1L;
            Long chatId = 1L;

            ChatMessage message = ChatMessage.builder()
                    .user(user1)
                    .chatRoom(chatRoom)
                    .contents("메시지")
                    .build();
            setId(message, chatId);

            given(chatMessageRepository.findById(chatId)).willReturn(Optional.of(message));

            // when
            chatService.deleteMessage(userId, chatRoomId, chatId);

            // then
            verify(chatMessageRepository, times(1)).delete(message);
            verify(messagingTemplate, times(1))
                    .convertAndSend(eq("/topic/chat-rooms/" + chatRoomId + "/updates"),
                            any(MessageDeletedResponseDto.class));
        }

        @Test
        void 남의_메세지를_삭제하려는_경우() {
            // given
            Long userId = 1L;
            Long chatRoomId = 1L;
            Long chatId = 1L;

            ChatMessage message = ChatMessage.builder()
                    .user(user2)  // 다른 사용자의 메시지
                    .chatRoom(chatRoom)
                    .contents("메시지")
                    .build();
            setId(message, chatId);

            given(chatMessageRepository.findById(chatId)).willReturn(Optional.of(message));

            // when & then
            assertThatThrownBy(() -> chatService.deleteMessage(userId, chatRoomId, chatId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_DELETE_MESSAGE);
        }
    }

    @Nested
    class Redis_채팅방_열기_테스트 {

        @Test
        void 채팅방_열기() {
            // given
            Long userId = 1L;
            Long chatRoomId = 1L;
            Long latestMessageId = 100L;

            given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
            given(chatMessageRepository.findLatestMessageId(chatRoomId))
                    .willReturn(Optional.of(latestMessageId));
            given(chatParticipantsRepository.findByUserIdAndChatRoomId(userId, chatRoomId))
                    .willReturn(Optional.of(participant1));

            // when
            chatService.openChatRoom(userId, chatRoomId);

            // then
            verify(redisChatManager, times(1)).markUserActive(chatRoomId, userId);
            verify(chatParticipantsRepository, times(1)).save(participant1);
        }
    }

    @Nested
    class Redis_채팅방_닫기 {

        @Test
        void 채팅방_닫기() {
            // given
            Long userId = 1L;
            Long chatRoomId = 1L;

            given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));

            // when
            chatService.closeChatRoom(userId, chatRoomId);

            // then
            verify(redisChatManager, times(1)).markUserInactive(chatRoomId, userId);
        }
    }

    @Nested
    class 채팅방_갱신_테스트 {

        @Test
        void 채팅방_갱신() {
            // given
            Long userId = 1L;
            Long chatRoomId = 1L;

            given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));

            // when
            chatService.refreshChatRoom(userId, chatRoomId);

            // then
            verify(redisChatManager, times(1)).refreshUserActivity(chatRoomId, userId);
        }
    }

    @Nested
    class 채팅방_탈퇴_테스트 {

        @Test
        void 채팅방_탈퇴() {
            // given
            Long userId = 1L;
            Long chatRoomId = 1L;

            given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
            given(chatParticipantsRepository.countByChatRoomId(chatRoomId)).willReturn(1L);

            // when
            chatService.leaveChatRoom(userId, chatRoomId);

            // then
            verify(chatParticipantsRepository, times(1))
                    .deleteByUserIdAndChatRoomId(userId, chatRoomId);
        }

        @Test
        void 채팅방_참여자가_존재하지_않으면_채팅방_삭제() {
            // given
            Long userId = 1L;
            Long chatRoomId = 1L;

            given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
            given(chatParticipantsRepository.countByChatRoomId(chatRoomId)).willReturn(0L);

            // when
            chatService.leaveChatRoom(userId, chatRoomId);

            // then
            verify(chatRoomRepository, times(1)).delete(chatRoom);
        }
    }
}
