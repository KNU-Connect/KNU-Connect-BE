package com.example.knu_connect.unit.mentor.service;

import com.example.knu_connect.domain.mentor.MentorService;
import com.example.knu_connect.domain.mentor.dto.response.MentorDetailResponseDto;
import com.example.knu_connect.domain.mentor.dto.response.MentorListResponseDto;
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.domain.user.entity.enums.*;
import com.example.knu_connect.domain.user.repository.UserRepository;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MentorServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks MentorService mentorService;

    private User mockUser;

    @Test
    @DisplayName("멘토 목록 조회 성공")
    void getMentorList_Success() {
        // given
        mockUser = mock(User.class);
        when(mockUser.getName()).thenReturn("홍길동");
        when(mockUser.getDepartment()).thenReturn(Department.computer);
        when(mockUser.getStatus()).thenReturn(Status.graduate);
        when(mockUser.getCareer()).thenReturn(Career.employment);
        when(mockUser.getInterest()).thenReturn(Interest.backend);
        when(mockUser.getMbti()).thenReturn(Mbti.ENFP);
        when(mockUser.getIntroduction()).thenReturn("안녕하세요");

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(mockUser), pageable, 1);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // when
        MentorListResponseDto dto = mentorService.getMentorList("employment", "backend", "홍", pageable);

        // then
        assertThat(dto.mentors()).hasSize(1);

        // 일부 필드만 확인
        assertThat(dto.mentors().get(0).name()).isEqualTo("홍길동");
        assertThat(dto.mentors().get(0).department()).isEqualTo("computer");
        assertThat(dto.mentors().get(0).mbti()).isEqualTo("ENFP");
        assertThat(dto.page()).isEqualTo(0);
        assertThat(dto.size()).isEqualTo(1);
        assertThat(dto.hasNext()).isFalse();

        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("멘토 상세 조회 성공")
    void getMentorDetail_Success() {
        // given
        mockUser = mock(User.class);
        when(mockUser.getName()).thenReturn("홍길동");
        when(mockUser.getDepartment()).thenReturn(Department.computer);
        when(mockUser.getStatus()).thenReturn(Status.graduate);
        when(mockUser.getCareer()).thenReturn(Career.employment);
        when(mockUser.getInterest()).thenReturn(Interest.backend);
        when(mockUser.getMbti()).thenReturn(Mbti.ENFP);
        when(mockUser.getIntroduction()).thenReturn("안녕하세요");
        when(mockUser.getDetailIntroduction()).thenReturn("저는 5년간 백엔드 개발을 해왔습니다.");

        when(userRepository.findByIdAndMentor(1L, true))
                .thenReturn(Optional.of(mockUser));

        // when
        MentorDetailResponseDto result = mentorService.getMentorDetail(1L);

        // then
        // 일부 필드만 확인
        assertThat(result.name()).isEqualTo("홍길동");
        assertThat(result.career()).isEqualTo("employment");
        assertThat(result.interest()).isEqualTo("backend");
        assertThat(result.mbti()).isEqualTo("ENFP");
        assertThat(result.detailIntroduction()).isEqualTo("저는 5년간 백엔드 개발을 해왔습니다.");

        verify(userRepository).findByIdAndMentor(1L, true);
    }

    @Test
    @DisplayName("멘토 상세 조회 실패 - 멘토 없음")
    void getMentorDetail_MentorNotFound_ThrowsException() {
        // given
        when(userRepository.findByIdAndMentor(1L, true)).thenReturn(Optional.empty());

        // when
        BusinessException exception = assertThrows(BusinessException.class, () ->
                mentorService.getMentorDetail(1L)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MENTOR_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.MENTOR_NOT_FOUND.message);

        verify(userRepository).findByIdAndMentor(1L, true);
    }
}
