package com.example.knu_connect.domain.chat.repository;

import com.example.knu_connect.domain.chat.entitiy.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
            "JOIN cr.participants p " +
            "WHERE p.user.id = :userId " +
            "ORDER BY cr.createdAt DESC")
    List<ChatRoom> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
            "JOIN FETCH cr.participants p " +
            "JOIN FETCH p.user " +
            "WHERE cr.id IN :chatRoomIds")
    List<ChatRoom> findAllWithParticipantsAndUsers(@Param("chatRoomIds") List<Long> chatRoomIds);

    @Query("SELECT cr FROM ChatRoom cr " +
            "JOIN cr.participants p1 " +
            "JOIN cr.participants p2 " +
            "WHERE p1.user.id = :userId1 " +
            "AND p2.user.id = :userId2 " +
            "AND p1.chatRoom = p2.chatRoom " +
            "AND NOT EXISTS (SELECT n FROM Networking n WHERE n.chatRoom = cr)") // 이 조건이 핵심
    Optional<ChatRoom> findByTwoParticipants(@Param("userId1") Long userId1,
                                             @Param("userId2") Long userId2);
}
