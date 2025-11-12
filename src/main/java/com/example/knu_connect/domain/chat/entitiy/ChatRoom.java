package com.example.knu_connect.domain.chat.entitiy;

import com.example.knu_connect.domain.basetime.CreatedTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_room")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
public class ChatRoom extends CreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatParticipants> participants = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    // 참여자 추가
    public void addParticipant(ChatParticipants participant) {
        this.participants.add(participant);
    }

    // 특정 사용자가 참여자인지 확인
    public boolean hasParticipant(Long userId) {
        return participants.stream()
                .anyMatch(p -> p.getUserId().equals(userId));
    }

    // 상대방 ID 가져오기 (단일)
    public Long getOtherParticipantId(Long myUserId) {
        return participants.stream()
                .map(ChatParticipants::getUserId)
                .filter(id -> !id.equals(myUserId))
                .findFirst()
                .orElse(null);
    }

    // 상대방 ID 목록 가져오기 (다중)
    public List<Long> getOtherParticipantIds(Long myUserId) {
        return participants.stream()
                .map(ChatParticipants::getUserId)
                .filter(id -> !id.equals(myUserId))
                .toList();
    }

    // 정적 팩토리 메서드
    public static ChatRoom create() {
        return new ChatRoom();
    }
}
