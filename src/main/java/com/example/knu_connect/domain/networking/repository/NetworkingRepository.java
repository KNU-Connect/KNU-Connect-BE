package com.example.knu_connect.domain.networking.repository;

import com.example.knu_connect.domain.networking.entitiy.Networking;
import com.example.knu_connect.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NetworkingRepository extends JpaRepository<Networking, Long> {

    // ChatRoom과 Participants를 함께 로딩
    @EntityGraph(attributePaths = {"chatRoom", "chatRoom.participants"})
    Page<Networking> findAll(Pageable pageable);

    // 키워드 검색 (제목 + 내용)
    @EntityGraph(attributePaths = {"chatRoom", "chatRoom.participants"})
    Page<Networking> findByTitleContainingOrContentsContaining(String title, String contents, Pageable pageable);

    // 내 네트워킹 조회
    @EntityGraph(attributePaths = {"chatRoom", "chatRoom.participants"})
    Page<Networking> findByUser(User user, Pageable pageable);

    boolean existsByUserIdAndChatRoomId(Long userId, Long chatRoomId);

    Optional<Networking> findByChatRoomId(Long chatRoomId);
}