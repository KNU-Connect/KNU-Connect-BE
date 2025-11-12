package com.example.knu_connect.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisChatManager {

    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String ACTIVE_USER_PREFIX = "chat:active:";
    private static final long ACTIVITY_TIMEOUT_MINUTES = 5;


    // 채팅방에 사용자가 활성화 상태임을 표시 (5분)
    public void markUserActive(Long chatRoomId, Long userId) {
        String key = getActiveUserKey(chatRoomId, userId);
        redisTemplate.opsForValue().set(key, "active", ACTIVITY_TIMEOUT_MINUTES, TimeUnit.MINUTES);
    }


    // 채팅방에서 사용자 활성화 상태 제거
    public void markUserInactive(Long chatRoomId, Long userId) {
        String key = getActiveUserKey(chatRoomId, userId);
        redisTemplate.delete(key);
    }


    // 사용자가 채팅방에서 활성화 상태인지 확인
    public boolean isUserActive(Long chatRoomId, Long userId) {
        String key = getActiveUserKey(chatRoomId, userId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }


    // 채팅방 활성화 상태 갱신 (TTL 연장)
    public void refreshUserActivity(Long chatRoomId, Long userId) {
        String key = getActiveUserKey(chatRoomId, userId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.expire(key, ACTIVITY_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        }
    }

    private String getActiveUserKey(Long chatRoomId, Long userId) {
        return ACTIVE_USER_PREFIX + chatRoomId + ":" + userId;
    }
}
