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
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantsRepository chatParticipantsRepository;

    @Override
    @Transactional
    public void createNetworking(User user, NetworkingCreateRequestDto request, Long chatRoomId) {
        User leader;
        ChatRoom chatRoom;

        if (chatRoomId == null) {
            // 새로운 채팅방 생성
            leader = user;
            chatRoom = ChatRoom.create();
            chatRoom = chatRoomRepository.save(chatRoom);

            // 생성자를 참여자로 추가
            ChatParticipants participants = ChatParticipants.builder()
                    .chatRoom(chatRoom)
                    .user(user)
                    .build();
            participants = chatParticipantsRepository.save(participants);

            chatRoom.addParticipant(participants);
        } else {
            // 기존 채팅방 사용
            leader = userRepository.findById(request.representativeId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            chatRoom = chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

            // 채팅방에 대표자가 참여자인지 확인
            boolean isParticipant = chatRoom.getParticipants().stream()
                    .anyMatch(p -> p.getUser().getId().equals(leader.getId()));

            if (!isParticipant) {
                throw new BusinessException(ErrorCode.CHAT_PARTICIPANTS_NOT_FOUND);
            }
        }

        // 네트워킹 생성 및 저장
        Networking networking = Networking.builder()
                .title(request.title())
                .contents(request.contents())
                .maxNumber(request.maxNumber())
                .user(leader)
                .chatRoom(chatRoom)
                .build();

        networkingRepository.save(networking);
    }

    @Override
    public NetworkingListResponseDto getNetworkingList(User user, String keyword, Pageable pageable) {
        Page<Networking> networkings;

        if (keyword == null || keyword.trim().isEmpty()) {
            // 키워드가 없으면 전체 조회
            networkings = networkingRepository.findAll(pageable);
        } else {
            // 키워드로 제목 또는 내용 검색
            networkings = networkingRepository.findByTitleContainingOrContentsContaining(
                    keyword, keyword, pageable);
        }

        List<NetworkingListResponseDto.NetworkingBoardDto> boards = networkings.stream()
                .map(n -> new NetworkingListResponseDto.NetworkingBoardDto(
                        n.getId(),
                        n.getTitle(),
                        n.getContents(),
                        n.getChatRoom().getParticipants().size(),
                        n.getMaxNumber(),
                        n.getCreatedAt()
                ))
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
                networking.getChatRoom().getParticipants().size(),
                networking.getMaxNumber(),
                networking.getCreatedAt(),
                representativeDto
        );
    }

    @Override
    @Transactional
    public void updateNetworking(User user, NetworkingUpdateRequestDto request, Long networkingId) {
        Networking networking = networkingRepository.findById(networkingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NETWORKING_NOT_FOUND));

        // 작성자 본인만 수정 가능
        if (!networking.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NETWORKING_FORBIDDEN);
        }

        // 네트워킹 정보 업데이트
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
                .map(n -> new MyNetworkingListResponseDto.MyNetworkingBoardDto(
                        n.getId(),
                        n.getTitle(),
                        n.getUser().getName(),
                        n.getContents(),
                        n.getChatRoom().getParticipants().size(),
                        n.getMaxNumber(),
                        n.getCreatedAt()
                ))
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
}