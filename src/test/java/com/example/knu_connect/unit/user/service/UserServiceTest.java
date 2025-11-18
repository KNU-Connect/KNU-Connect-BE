package com.example.knu_connect.unit.user.service;

import com.example.knu_connect.domain.user.dto.request.UserUpdateRequestDto;
import com.example.knu_connect.domain.user.dto.response.UserInfoResponseDto;
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.domain.user.entity.enums.*;
import com.example.knu_connect.domain.user.repository.UserRepository;
import com.example.knu_connect.domain.user.service.UserService;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserService userService;

    @Test
    @DisplayName("유저 정보 조회 성공")
    void getUserInfo_Success() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .name("홍길동")
                .email("test@knu.ac.kr")
                .password("1234")
                .status(Status.student)
                .department(Department.computer)
                .career(Career.employment)
                .interest(Interest.backend)
                .mbti(Mbti.ENFP)
                .mentor(false)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // when
        UserInfoResponseDto response = userService.getUserInfo(userId);

        // then
        assertThat(response.name()).isEqualTo("홍길동");
        assertThat(response.status()).isEqualTo("student");
        assertThat(response.department()).isEqualTo("computer");
        assertThat(response.career()).isEqualTo("employment");
        assertThat(response.mbti()).isEqualTo("ENFP");
        assertThat(response.interest()).isEqualTo("backend");
        assertThat(response.mentor()).isEqualTo(false);
        assertThat(response.introduction()).isEqualTo(null);
        assertThat(response.detailIntroduction()).isEqualTo(null);
    }

    @Test
    @DisplayName("유저 정보 조회 실패 - 유저 없음")
    void getUserInfo_NotFound_ThrowsException() {
        // given
        Long userId = 1L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.getUserInfo(userId));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.USER_NOT_FOUND.message);
    }

    @Test
    @DisplayName("유저 정보 수정 성공")
    void updateUserInfo_Success() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .name("홍길동")
                .email("test@knu.ac.kr")
                .password("1234")
                .status(Status.student)
                .department(Department.computer)
                .career(Career.employment)
                .interest(Interest.backend)
                .mbti(Mbti.ENFP)
                .mentor(false)
                .build();

        UserUpdateRequestDto dto = new UserUpdateRequestDto(
                null,
                null,
                "ISFP",
                null,
                null,
                true,
                "new intro",
                "new detailIntro"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // when
        userService.updateUserInfo(userId, dto);

        // then
        // 업데이트 된 값
        assertThat(user.getMbti()).isEqualTo(Mbti.ISFP);
        assertThat(user.isMentor()).isTrue();
        assertThat(user.getIntroduction()).isEqualTo("new intro");
        assertThat(user.getDetailIntroduction()).isEqualTo("new detailIntro");
        // 업데이트 되지 않은 값
        assertThat(user.getDepartment()).isEqualTo(Department.computer);
        assertThat(user.getCareer()).isEqualTo(Career.employment);
        assertThat(user.getStatus()).isEqualTo(Status.student);
        assertThat(user.getInterest()).isEqualTo(Interest.backend);
    }

    @Test
    @DisplayName("유저 정보 수정 실패 - 유저 없음")
    void updateUserInfo_NotFound_ThrowsException() {
        // given
        Long userId = 1L;
        UserUpdateRequestDto dto = new UserUpdateRequestDto(
                null, null, null, null, null, null, null, null
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.updateUserInfo(userId, dto));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.USER_NOT_FOUND.message);
    }
}
