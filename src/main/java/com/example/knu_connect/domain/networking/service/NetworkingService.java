package com.example.knu_connect.domain.networking.service;

import com.example.knu_connect.domain.networking.dto.request.NetworkingCreateRequestDto;
import com.example.knu_connect.domain.networking.dto.request.NetworkingUpdateRequestDto;
import com.example.knu_connect.domain.networking.dto.response.MyNetworkingListResponseDto;
import com.example.knu_connect.domain.networking.dto.response.NetworkingDetailResponseDto;
import com.example.knu_connect.domain.networking.dto.response.NetworkingListResponseDto;
import com.example.knu_connect.domain.networking.dto.response.ParticipantsResponseDto;
import com.example.knu_connect.domain.user.entity.User;
import org.springframework.data.domain.Pageable;

public interface NetworkingService {

    void createNetworking(User user, NetworkingCreateRequestDto request, Long chatRoomId);

    NetworkingListResponseDto getNetworkingList(User user, String keyword, Pageable pageable);

    NetworkingDetailResponseDto getNetworkingDetail(User user, Long networkingId);

    void updateNetworking(User user, NetworkingUpdateRequestDto request, Long networkingId);

    MyNetworkingListResponseDto getMyNetworkingList(User user, Pageable pageable);

    ParticipantsResponseDto getNetworkingParticipants(User user, Long networkingId);
}
