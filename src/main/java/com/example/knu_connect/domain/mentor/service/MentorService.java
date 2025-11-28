package com.example.knu_connect.domain.mentor.service;

import com.example.knu_connect.domain.mentor.dto.response.MentorDetailResponseDto;
import com.example.knu_connect.domain.mentor.dto.response.MentorListResponseDto;
import com.example.knu_connect.domain.mentor.specification.MentorSpecification;
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.domain.user.repository.UserRepository;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MentorService {

    private final UserRepository userRepository;

    public MentorListResponseDto getMentorList(String career, String interest, String keyword, Pageable pageable) {

        Specification<User> spec =
                Specification.allOf(
                        MentorSpecification.isMentor(),
                        MentorSpecification.hasCareer(career),
                        MentorSpecification.hasInterest(interest),
                        MentorSpecification.containsKeyword(keyword)
                );

        Page<User> result = userRepository.findAll(spec, pageable);

        List<MentorListResponseDto.MentorDto> mentors = result.getContent().stream()
                .map(user -> new MentorListResponseDto.MentorDto(
                        user.getId(),
                        user.getName(),
                        user.getDepartment().name(),
                        user.getStatus().name(),
                        user.getCareer().name(),
                        user.getInterest().name(),
                        user.getMbti().name(),
                        user.getIntroduction()
                ))
                .toList();

        return new MentorListResponseDto(
                mentors,
                result.getNumber(),               // page
                result.getNumberOfElements(),     // size
                result.hasNext()
        );
    }

    public MentorDetailResponseDto getMentorDetail(Long userId) {
        User user = userRepository.findByIdAndMentor(userId, true)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENTOR_NOT_FOUND));

        return new MentorDetailResponseDto(
                user.getId(),
                user.getName(),
                user.getDepartment().name(),
                user.getStatus().name(),
                user.getCareer().name(),
                user.getInterest().name(),
                user.getMbti().name(),
                user.getIntroduction(),
                user.getDetailIntroduction()
        );
    }
}
