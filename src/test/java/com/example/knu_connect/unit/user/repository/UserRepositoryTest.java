package com.example.knu_connect.unit.user.repository;

import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.domain.user.entity.enums.*;
import com.example.knu_connect.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일로 존재 여부를 확인할 수 있다")
    void existsByEmail_ReturnsTrue() {
        // given
        User user = User.builder()
                .name("홍길동")
                .email("test@knu.ac.kr")
                .password("1234")
                .status(Status.student)
                .department(Department.computer)
                .career(Career.employment)
                .interest(Interest.backend)
                .mbti(Mbti.ISFP)
                .mentor(false)
                .build();

        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByEmail("test@knu.ac.kr");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("등록되지 않은 이메일은 false를 반환한다")
    void existsByEmail_ReturnsFalse() {
        // given
        // when
        boolean exists = userRepository.existsByEmail("test@knu.ac.kr");

        // then
        assertThat(exists).isFalse();

    }
}
