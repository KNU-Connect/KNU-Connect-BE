package com.example.knu_connect.domain.user.service;

import com.example.knu_connect.domain.user.dto.request.UserUpdateRequestDto;
import com.example.knu_connect.domain.user.dto.response.UserInfoResponseDto;
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.domain.user.repository.UserRepository;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserInfoResponseDto getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new UserInfoResponseDto(
                user.getName(),
                user.getStatus().name(),
                user.getDepartment().name(),
                user.getCareer().name(),
                user.getMbti().name(),
                user.getInterest().name(),
                user.isMentor(),
                user.getIntroduction(),
                user.getDetailIntroduction()
        );
    }

    @Transactional
    public void updateUserInfo(Long userId, UserUpdateRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.update(
                dto.department(),
                dto.career(),
                dto.mbti(),
                dto.status(),
                dto.interest(),
                dto.mentor(),
                dto.introduction(),
                dto.detailIntroduction()
        );
    }
}
