package com.example.knu_connect.domain.user.entity;

import com.example.knu_connect.domain.user.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 유저 아이디 (PK)

    @Column(name = "name", nullable = false, length = 255)
    private String name;  // 사용자 이름

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;  // 이메일

    @Column(name = "password", nullable = false, length = 255)
    private String password;  // 비밀번호

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 255)
    private Status status;  // 재학 구분

    @Enumerated(EnumType.STRING)
    @Column(name = "department", nullable = false, length = 255)
    private Department department;  // 학과

    @Enumerated(EnumType.STRING)
    @Column(name = "career", nullable = false, length = 255)
    private Career career;  // 진로

    @Enumerated(EnumType.STRING)
    @Column(name = "interest", nullable = false, length = 255)
    private Interest interest;  // 관심분야

    @Enumerated(EnumType.STRING)
    @Column(name = "mbti", nullable = false, length = 255)
    private Mbti mbti;  // MBTI

    @Column(name = "mentor", nullable = false)
    private boolean mentor;  // 멘토 여부

    @Column(name = "introduction", length = 255)
    private String introduction;  // 한줄 소개

    @Column(name = "detail_introduction", length = 1000)
    private String detailIntroduction;  // 상세 소개

    @Builder
    public User(String name, String email, String password, Status status, Department department,
                Career career, Interest interest, Mbti mbti, boolean mentor) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.status = status;
        this.department = department;
        this.career = career;
        this.interest = interest;
        this.mbti = mbti;
        this.mentor = mentor;
    }
}