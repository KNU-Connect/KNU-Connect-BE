package com.example.knu_connect.global.auth.websocket;

import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.domain.user.repository.UserRepository;
import com.example.knu_connect.global.auth.jwt.CustomUserDetails;
import com.example.knu_connect.global.auth.jwt.CustomUserDetailsService;
import com.example.knu_connect.global.auth.jwt.JwtUtil;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            // CONNECT 시 인증 처리
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                authenticateUser(accessor);
            } 
            // SEND, SUBSCRIBE 등 다른 명령어에서도 인증 정보 유지
            else if (accessor.getUser() != null) {
                // 이미 인증된 사용자 정보가 있으면 SecurityContext에 설정
                if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken) {
                    UsernamePasswordAuthenticationToken auth = 
                        (UsernamePasswordAuthenticationToken) accessor.getUser();
                    
                    // SecurityContext에 저장 (ThreadLocal)
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    
                    // Message Header에도 저장 (스레드 간 공유 가능)
                    accessor.setHeader("simpUser", auth);
                    
                    if (auth.getPrincipal() instanceof CustomUserDetails) {
                        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
                        log.debug("WebSocket 메시지 처리 - 사용자: {}, 명령: {}", 
                            userDetails.getUsername(), accessor.getCommand());
                    }
                }
            }
        }

        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            StompCommand command = accessor.getCommand();

            // DISCONNECT 시에만 SecurityContext 정리
            if (StompCommand.DISCONNECT.equals(command)) {
                SecurityContextHolder.clearContext();
                log.info("SecurityContext cleared - 사용자 연결 종료");
            }
        }
    }

    // 소켓 연결 전 헤더에서 토큰을 가져와 검증
    private void authenticateUser(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Claims claims = jwtUtil.getClaims(token);
                String tokenType = claims.get(JwtUtil.TOKEN_TYPE_CLAIM, String.class);

                if (JwtUtil.ACCESS_TOKEN_TYPE.equals(tokenType)) {
                    String email = claims.getSubject();

                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS));

                    CustomUserDetails customUserDetails = new CustomUserDetails(user);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            customUserDetails, null, customUserDetails.getAuthorities());

                    // SecurityContext에 인증 정보 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // WebSocket 세션에 사용자 정보 저장 (이후 메시지에서 재사용)
                    accessor.setUser(authentication);

                    log.info("WebSocket 연결 인증 성공 - 사용자: {}", email);
                } else {
                    log.warn("WebSocket 인증 실패 - 잘못된 토큰 타입");
                    throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
                }
            } catch (Exception e) {
                log.warn("WebSocket 인증 실패: {}", e.getMessage());
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }
        } else {
            log.warn("WebSocket 연결 시도 - Authorization 헤더 없음");
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }
}
