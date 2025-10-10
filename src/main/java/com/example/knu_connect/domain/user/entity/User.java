package com.example.knu_connect.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "user")
@Entity
@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 유저 아이디 (PK)

    @Column(name = "name", nullable = false, length = 255)
    private String name;  // 사용자 이름

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;  // 이메일

    @Column(name = "password", nullable = false, length = 255)
    private String password;  // 비밀번호

    @Column(name = "status", nullable = false, length = 255)
    private String status;  // 재학 구분

    @Column(name = "department", nullable = false, length = 255)
    private String department;  // 학과

    @Column(name = "career", nullable = false, length = 255)
    private String career;  // 진로

    @Column(name = "interest", nullable = false, length = 255)
    private String interest;  // 관심분야

    @Column(name = "mbti", nullable = false, length = 255)
    private String mbti;  // MBTI

    @Column(name = "mentor", nullable = false)
    private boolean mentor;  // 멘토 여부

    @Column(name = "introduction", length = 255)
    private String introduction;  // 한줄 소개

    @Column(name = "detail_introduction", length = 1000)
    private String detailIntroduction;  // 상세 소개

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;  // 수정일자


    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}