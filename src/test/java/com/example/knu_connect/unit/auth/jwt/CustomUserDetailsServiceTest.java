package com.example.knu_connect.unit.auth.jwt;

import com.example.knu_connect.global.auth.jwt.CustomUserDetails;
import com.example.knu_connect.global.auth.jwt.CustomUserDetailsService;
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    @Mock private UserRepository userRepository;
    @InjectMocks private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("이메일로 유저 조회 성공")
    void loadUserByUsername_Success() {
        // given
        String email = "test@knu.ac.kr";
        User mockUser = mock(User.class);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // then
        assertThat(userDetails).isInstanceOf(CustomUserDetails.class);
        assertThat(((CustomUserDetails) userDetails).getUser()).isEqualTo(mockUser);
    }

    @Test
    @DisplayName("이메일로 유저 조회 실패 - 회원이 존재하지 않음")
    void loadUserByUsername_userNotFound_throwsException() {
        // given
        String email = "test@knu.ac.kr";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername(email)
        );

        // then
        assertThat(exception).isInstanceOf(UsernameNotFoundException.class);
        assertThat(exception.getMessage()).isEqualTo("User not found with email: " + email);
    }
}
