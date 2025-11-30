package com.example.knu_connect.unit.networking;

import com.example.knu_connect.domain.chat.entitiy.ChatParticipants;
import com.example.knu_connect.domain.chat.entitiy.ChatRoom;
import com.example.knu_connect.domain.chat.repository.ChatParticipantsRepository;
import com.example.knu_connect.domain.chat.repository.ChatRoomRepository;
import com.example.knu_connect.domain.networking.dto.request.NetworkingCreateRequestDto;
import com.example.knu_connect.domain.networking.dto.request.NetworkingUpdateRequestDto;
import com.example.knu_connect.domain.networking.dto.response.MyNetworkingListResponseDto;
import com.example.knu_connect.domain.networking.dto.response.NetworkingDetailResponseDto;
import com.example.knu_connect.domain.networking.dto.response.NetworkingListResponseDto;
import com.example.knu_connect.domain.networking.dto.response.ParticipantsResponseDto;
import com.example.knu_connect.domain.networking.entitiy.Networking;
import com.example.knu_connect.domain.networking.repository.NetworkingRepository;
import com.example.knu_connect.domain.networking.service.NetWorkingServiceImpl;
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.domain.user.entity.enums.*;
import com.example.knu_connect.domain.user.repository.UserRepository;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NetworkingServiceImplTest {

    @InjectMocks
    private NetWorkingServiceImpl networkingService;

    @Mock
    private NetworkingRepository networkingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatParticipantsRepository chatParticipantsRepository;

    private User user;
    private ChatRoom chatRoom;
    private Networking networking;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("Test User")
                .email("test@knu.ac.kr")
                .password("password")
                .status(Status.student)
                .department(Department.computer)
                .career(Career.employment)
                .interest(Interest.backend)
                .mbti(Mbti.ENFP)
                .mentor(false)
                .build();
        setId(user, 1L);

        chatRoom = ChatRoom.create();
        setId(chatRoom, 1L);

        networking = Networking.builder()
                .title("Test Title")
                .contents("Test Contents")
                .maxNumber(5)
                .user(user)
                .chatRoom(chatRoom)
                .curNumber(1)
                .visible(true)
                .build();
        setId(networking, 1L);

        ChatParticipants participant = ChatParticipants.builder()
                .user(user)
                .chatRoom(chatRoom)
                .lastReadMessageId(0L)
                .build();
        chatRoom.addParticipant(participant);
    }

    private void setId(Object target, Long id) {
        try {
            Field field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("ID 설정 실패", e);
        }
    }

    @Nested
    class 네트워킹_생성_테스트 {

        @Test
        void 네트워킹_페이지에서_생성() {
            // given
            NetworkingCreateRequestDto request = new NetworkingCreateRequestDto(
                    "Title", "Contents", 5, null // representativeId는 서비스 로직에서 무시됨
            );

            given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(chatRoom);

            // when
            networkingService.createNetworking(user, request, null);

            // then
            verify(chatRoomRepository).save(any(ChatRoom.class));
            verify(chatParticipantsRepository).save(any(ChatParticipants.class));
            verify(networkingRepository).save(any(Networking.class));
        }

        @Test
        @DisplayName("채팅방에서 생성 시 - 새로운 채팅방이 생성되고 대표자와 본인이 참여해야 한다")
        void createNetworking_FromChatRoom_CreatesNewRoom() {
            // given
            Long existingChatRoomId = 1L;
            Long representativeId = 2L;

            User mentor = User.builder().email("mentor@test.com").build();
            setId(mentor, representativeId);

            NetworkingCreateRequestDto request = new NetworkingCreateRequestDto(
                    "Title", "Contents", 5, representativeId
            );

            given(userRepository.findById(representativeId)).willReturn(Optional.of(mentor));

            // when
            networkingService.createNetworking(user, request, existingChatRoomId);

            // then
            ArgumentCaptor<ChatRoom> chatRoomCaptor = ArgumentCaptor.forClass(ChatRoom.class);
            verify(chatRoomRepository).save(chatRoomCaptor.capture());
            ChatRoom createdChatRoom = chatRoomCaptor.getValue(); // 캡처된 실제 객체

            verify(chatParticipantsRepository, times(2)).save(any(ChatParticipants.class));

            ArgumentCaptor<Networking> networkingCaptor = ArgumentCaptor.forClass(Networking.class);
            verify(networkingRepository).save(networkingCaptor.capture());

            Networking savedNetworking = networkingCaptor.getValue();

            assertThat(savedNetworking.getChatRoom()).isEqualTo(createdChatRoom);
            assertThat(savedNetworking.getChatRoom().getId()).isNotEqualTo(existingChatRoomId);
            assertThat(savedNetworking.getUser()).isEqualTo(mentor);
        }
    }
    
    @Nested
    class 네트워킹_목록조회_테스트 {

        @Test
        void 전체목록조회() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Networking> page = new PageImpl<>(List.of(networking));

            given(networkingRepository.findAll(pageable)).willReturn(page);

            // when
            NetworkingListResponseDto response = networkingService.getNetworkingList(user, null, pageable);

            // then
            assertThat(response.boards()).hasSize(1);
            assertThat(response.boards().get(0).title()).isEqualTo("Test Title");
            assertThat(response.boards().get(0).curNumber()).isEqualTo(1);
        }

        @Test
        void 키워드있는_전체목록조회() {
            // given
            String keyword = "Test";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Networking> page = new PageImpl<>(List.of(networking));

            given(networkingRepository.findByTitleContainingOrContentsContaining(keyword, keyword, pageable))
                    .willReturn(page);

            // when
            NetworkingListResponseDto response = networkingService.getNetworkingList(user, keyword, pageable);

            // then
            assertThat(response.boards()).hasSize(1);
            verify(networkingRepository).findByTitleContainingOrContentsContaining(keyword, keyword, pageable);
        }
    }

    @Nested
    class 네트워킹_상세조회_테스트 {

        @Test
        void 상세조회() {
            // given
            Long networkingId = 1L;
            given(networkingRepository.findById(networkingId)).willReturn(Optional.of(networking));

            // when
            NetworkingDetailResponseDto response = networkingService.getNetworkingDetail(user, networkingId);

            // then
            assertThat(response.title()).isEqualTo(networking.getTitle());
            assertThat(response.representative().name()).isEqualTo(user.getName());
            assertThat(response.curNumber()).isEqualTo(1); // 실제 참여자 수
        }

        @Test
        void 상세조회_실패() {
            // given
            Long networkingId = 999L;
            given(networkingRepository.findById(networkingId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> networkingService.getNetworkingDetail(user, networkingId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.NETWORKING_NOT_FOUND);
        }
    }

    @Nested
    class 네트워킹_수정 {

        @Test
        @DisplayName("수정 성공")
        void update_Success() {
            // given
            Long networkingId = 1L;
            NetworkingUpdateRequestDto request = new NetworkingUpdateRequestDto(
                    "Updated Title", "Updated Contents", 10, 1L
            );

            given(networkingRepository.findById(networkingId)).willReturn(Optional.of(networking));

            // when
            networkingService.updateNetworking(user, request, networkingId);

            // then
            assertThat(networking.getTitle()).isEqualTo("Updated Title");
            assertThat(networking.getContents()).isEqualTo("Updated Contents");
            assertThat(networking.getMaxNumber()).isEqualTo(10);
        }

        @Test
        void 네트워킹_수정_실패() {
            // given
            Long networkingId = 1L;
            User otherUser = User.builder().email("other@test.com").build();
            setId(otherUser, 2L);

            NetworkingUpdateRequestDto request = new NetworkingUpdateRequestDto(
                    "Updated Title", "Updated Contents", 10, 1L
            );

            given(networkingRepository.findById(networkingId)).willReturn(Optional.of(networking));

            // when & then
            assertThatThrownBy(() -> networkingService.updateNetworking(otherUser, request, networkingId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.NETWORKING_FORBIDDEN);
        }
    }

    @Nested
    class 참여자목록_조회_테스트 {

        @Test
        void 참여자_목록조회_테스트() {
            // given
            Long networkingId = 1L;
            given(networkingRepository.findById(networkingId)).willReturn(Optional.of(networking));

            // when
            ParticipantsResponseDto response = networkingService.getNetworkingParticipants(user, networkingId);

            // then
            assertThat(response.participants()).hasSize(1);
            assertThat(response.participants().get(0).name()).isEqualTo(user.getName());
        }
    }

    @Nested
    class 나의_네트워킹_조회_테스트 {
        @Test
        void 나의_네트워킹_조회() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Networking> page = new PageImpl<>(List.of(networking));

            given(networkingRepository.findByUser(user, pageable)).willReturn(page);

            // when
            MyNetworkingListResponseDto response = networkingService.getMyNetworkingList(user, pageable);

            // then
            assertThat(response.boards()).hasSize(1);
            assertThat(response.boards().get(0).writer()).isEqualTo(user.getName());
        }
    }
}