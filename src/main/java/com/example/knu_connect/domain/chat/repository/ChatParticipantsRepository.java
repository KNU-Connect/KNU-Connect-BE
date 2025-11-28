package com.example.knu_connect.domain.chat.repository;

import com.example.knu_connect.domain.chat.entitiy.ChatParticipants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatParticipantsRepository extends JpaRepository<ChatParticipants, Long> {

    @Modifying
    @Query("DELETE FROM ChatParticipants cp " +
            "WHERE cp.chatRoom.id = :chatRoomId " +
            "AND cp.user.id = :userId")
    void deleteByUserIdAndChatRoomId(@Param("userId") Long userId,
                                      @Param("chatRoomId") Long chatRoomId);

    @Query("SELECT COUNT(cp) FROM ChatParticipants cp " +
            "WHERE cp.chatRoom.id = :chatRoomId")
    Long countByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    @Query("SELECT cp FROM ChatParticipants cp " +
            "WHERE cp.chatRoom.id = :chatRoomId " +
            "AND cp.user.id = :userId")
    Optional<ChatParticipants> findByUserIdAndChatRoomId(@Param("userId") Long userId,
                                                          @Param("chatRoomId") Long chatRoomId);

    boolean existsByUser_IdAndChatRoom_Id(Long userId, Long chatRoomId);
}
