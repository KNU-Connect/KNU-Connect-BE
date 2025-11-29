package com.example.knu_connect.domain.networking.entitiy;

import com.example.knu_connect.domain.basetime.CreatedTimeEntity;
import com.example.knu_connect.domain.chat.entitiy.ChatRoom;
import com.example.knu_connect.domain.user.entity.User;
import com.example.knu_connect.global.exception.common.BusinessException;
import com.example.knu_connect.global.exception.common.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "networking")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Networking extends CreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ChatRoom chatRoom;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String contents;

    @Column(nullable = false)
    private Integer curNumber;

    @Column(nullable = false)
    private Integer maxNumber;

    @Column(nullable = false)
    private Boolean visible;

    @Builder
    public Networking(
            User user,
            ChatRoom chatRoom,
            String title,
            String contents,
            Integer curNumber,
            Integer maxNumber,
            Boolean visible)
    {
        this.user = user;
        this.chatRoom = chatRoom;
        this.title = title;
        this.contents = contents;
        this.curNumber = curNumber;
        this.maxNumber = maxNumber;
        this.visible = visible;
    }

    public void update(String title, String contents, Integer maxNumber)
    {
        this.title = title;
        this.contents = contents;

        if (maxNumber < this.curNumber)
            throw new BusinessException(ErrorCode.INVALID_MAX_NUMBER);
        this.maxNumber = maxNumber;
    }

    public void join() {
        if (this.curNumber >= this.maxNumber) {
            throw new BusinessException(ErrorCode.NETWORKING_FULL);
        }
        this.curNumber++;
    }

    public void leave() {
        if (this.curNumber > 0) {
            this.curNumber--;
        }
    }

}
