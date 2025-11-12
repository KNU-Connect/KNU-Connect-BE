package com.example.knu_connect.domain.chat.repository;

import com.example.knu_connect.domain.chat.entitiy.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 최신 메시지 조회 (cursor 기반 페이징)
    @Query("SELECT cm FROM ChatMessage cm " +
            "WHERE cm.chatRoom.id = :chatRoomId " +
            "AND (:cursor IS NULL OR cm.id < :cursor) " +
            "ORDER BY cm.id DESC")
    List<ChatMessage> findByChatRoomIdWithCursor(@Param("chatRoomId") Long chatRoomId,
                                                   @Param("cursor") Long cursor,
                                                   Pageable pageable);

    // 채팅방의 가장 최근 메시지 조회
    Optional<ChatMessage> findFirstByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);

    // 최신 메시지 ID 조회
    @Query("SELECT MAX(cm.id) FROM ChatMessage cm " +
            "WHERE cm.chatRoom.id = :chatRoomId")
    Optional<Long> findLatestMessageId(@Param("chatRoomId") Long chatRoomId);

    // 안읽은 메시지 개수 조회
    @Query("SELECT COUNT(cm) FROM ChatMessage cm " +
            "WHERE cm.chatRoom.id = :chatRoomId " +
            "AND cm.id > :lastReadMessageId " +
            "AND cm.user.id != :userId")
    Long countUnreadMessages(@Param("chatRoomId") Long chatRoomId,
                              @Param("lastReadMessageId") Long lastReadMessageId,
                              @Param("userId") Long userId);
}
