package com.example.knu_connect.domain.chat.entitiy;

import com.example.knu_connect.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_participants")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ChatParticipants {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;


    @Column(name = "last_read_message_id", nullable = false)
    Long lastReadMessageId;

    @Builder
    public ChatParticipants(User user, ChatRoom chatRoom, Long lastReadMessageId) {
        this.user = user;
        this.chatRoom = chatRoom;
        this.lastReadMessageId = lastReadMessageId;
    }

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public User getUser() {
        return user;
    }

    public void updateLastReadMessageId(Long messageId) {
        this.lastReadMessageId = messageId;
    }
}
