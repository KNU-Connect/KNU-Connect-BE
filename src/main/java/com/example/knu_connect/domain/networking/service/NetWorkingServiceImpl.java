package com.example.knu_connect.domain.networking.service;

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
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.domain.user.repository.UserRepository;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NetWorkingServiceImpl implements NetworkingService {

    private final NetworkingRepository networkingRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantsRepository chatParticipantsRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createNetworking(User user, NetworkingCreateRequestDto request, Long chatRoomId) {
        User leader;
        ChatRoom chatRoom;

        if (chatRoomId != null) {
            if (request.representativeId() == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "대표자 ID는 필수입니다.");
            }
            leader = userRepository.findById(request.representativeId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        } else {
            leader = user;
        }

        if (chatRoomId == null) {
            chatRoom = ChatRoom.create();
            chatRoom = chatRoomRepository.save(chatRoom);

            ChatParticipants participants = ChatParticipants.builder()
                    .chatRoom(chatRoom)
                    .user(leader)
                    .lastReadMessageId(0L)
                    .build();
            chatParticipantsRepository.save(participants);

            chatRoom.addParticipant(participants);

        } else {
            chatRoom = chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

            boolean isParticipant = chatRoom.getParticipants().stream()
                    .anyMatch(p -> p.getUser().getId().equals(leader.getId()));

            if (!isParticipant) {
                throw new BusinessException(ErrorCode.CHAT_PARTICIPANTS_NOT_FOUND, "지정된 대표자가 채팅방에 참여하고 있지 않습니다.");
            }
        }

        Networking networking = Networking.builder()
                .title(request.title())
                .contents(request.contents())
                .maxNumber(request.maxNumber())
                .user(leader)
                .chatRoom(chatRoom)
                .visible(true)
                .curNumber(chatRoom.getParticipants().size())
                .build();

        networkingRepository.save(networking);
    }

    @Override
    public NetworkingListResponseDto getNetworkingList(User user, String keyword, Pageable pageable) {
        Page<Networking> networkings;

        if (keyword == null || keyword.trim().isEmpty()) {
            networkings = networkingRepository.findAll(pageable);
        } else {
            networkings = networkingRepository.findByTitleContainingOrContentsContaining(
                    keyword, keyword, pageable);
        }

        List<NetworkingListResponseDto.NetworkingBoardDto> boards = networkings.stream()
                .map(n -> {

                    int realParticipantCount = n.getCurNumber();

                    boolean isParticipating = n.getChatRoom().getParticipants().stream()
                            .anyMatch(p -> p.getUserId().equals(user.getId()));

                    return new NetworkingListResponseDto.NetworkingBoardDto(
                            n.getId(),
                            n.getTitle(),
                            n.getContents(),
                            realParticipantCount,
                            n.getMaxNumber(),
                            isParticipating,
                            n.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());

        return new NetworkingListResponseDto(
                boards,
                networkings.getNumber(),
                networkings.getSize(),
                networkings.hasNext()
        );
    }

    @Override
    public NetworkingDetailResponseDto getNetworkingDetail(User user, Long networkingId) {
        Networking networking = networkingRepository.findById(networkingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NETWORKING_NOT_FOUND));

        User representative = networking.getUser();

        int realParticipantCount = networking.getCurNumber();

        boolean isParticipating = networking.getChatRoom().getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(user.getId()));

        NetworkingDetailResponseDto.RepresentativeDto representativeDto =
                new NetworkingDetailResponseDto.RepresentativeDto(
                        representative.getName(),
                        representative.getStatus().name(),
                        representative.getDepartment().name(),
                        representative.getCareer().name(),
                        representative.getMbti().name(),
                        representative.getInterest().name(),
                        representative.getIntroduction()
                );

        return new NetworkingDetailResponseDto(
                networking.getId(),
                networking.getTitle(),
                networking.getContents(),
                realParticipantCount,
                networking.getMaxNumber(),
                isParticipating,
                networking.getCreatedAt(),
                representativeDto
        );
    }

    @Override
    @Transactional
    public void updateNetworking(User user, NetworkingUpdateRequestDto request, Long networkingId) {
        Networking networking = networkingRepository.findById(networkingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NETWORKING_NOT_FOUND));

        if (!networking.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NETWORKING_FORBIDDEN);
        }

        networking.update(
                request.title(),
                request.contents(),
                request.maxNumber()
        );
    }

    @Override
    public MyNetworkingListResponseDto getMyNetworkingList(User user, Pageable pageable) {
        Page<Networking> networkings = networkingRepository.findByUser(user, pageable);

        List<MyNetworkingListResponseDto.MyNetworkingBoardDto> boards = networkings.stream()
                .map(n -> {
                    int realParticipantCount = n.getChatRoom().getParticipants().size();
                    return new MyNetworkingListResponseDto.MyNetworkingBoardDto(
                            n.getId(),
                            n.getTitle(),
                            n.getUser().getName(),
                            n.getContents(),
                            realParticipantCount,
                            n.getMaxNumber(),
                            n.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());

        return new MyNetworkingListResponseDto(
                boards,
                networkings.getNumber(),
                networkings.getSize(),
                networkings.hasNext()
        );
    }

    @Override
    public ParticipantsResponseDto getNetworkingParticipants(User user, Long networkingId) {
        Networking networking = networkingRepository.findById(networkingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NETWORKING_NOT_FOUND));

        ChatRoom chatRoom = networking.getChatRoom();
        List<ChatParticipants> participantsList = chatRoom.getParticipants();

        List<ParticipantsResponseDto.ParticipantDto> participants = participantsList.stream()
                .map(cp -> {
                    User participant = cp.getUser();
                    return new ParticipantsResponseDto.ParticipantDto(
                            participant.getName(),
                            participant.getStatus().name(),
                            participant.getDepartment().name(),
                            participant.getCareer().name(),
                            participant.getMbti().name(),
                            participant.getInterest().name(),
                            participant.getIntroduction()
                    );
                })
                .collect(Collectors.toList());

        return new ParticipantsResponseDto(participants);
    }

    @Transactional
    @Override
    public void joinNetworking(User user, Long networkingId) {
        Networking networking = networkingRepository.findById(networkingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NETWORKING_NOT_FOUND));

        ChatRoom chatRoom = networking.getChatRoom();

        if (chatParticipantsRepository.existsByUser_IdAndChatRoom_Id(user.getId(), chatRoom.getId())) {
            throw new BusinessException(ErrorCode.ALREADY_PARTICIPATED);
        }

        networking.join();

        ChatParticipants newParticipant = ChatParticipants.builder()
                .user(user)
                .chatRoom(chatRoom)
                .lastReadMessageId(0L)
                .build();

        chatParticipantsRepository.save(newParticipant);
        chatRoom.addParticipant(newParticipant);
    }
}