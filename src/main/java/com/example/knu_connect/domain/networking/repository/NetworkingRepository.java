package com.example.knu_connect.domain.networking.repository;

import com.example.knu_connect.domain.networking.entitiy.Networking;
import com.example.knu_connect.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NetworkingRepository extends JpaRepository<Networking, Long> {

    // 키워드 검색 (제목 + 내용)
    Page<Networking> findByTitleContainingOrContentsContaining(String title, String contents, Pageable pageable);

    // 내 네트워킹 조회
    Page<Networking> findByUser(User user, Pageable pageable);
}
