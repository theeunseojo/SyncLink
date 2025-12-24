package com.SyncLink.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "room_id", "member_id" }) // 같은 방에 같은 사람 중복 참여 방지
})
public class RoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 추가 정보
    private LocalDateTime joinedAt; // 참여 시점
    private Boolean isHost; // 방장 여부

    @Builder
    public RoomMember(Room room, Member member, Boolean isHost) {
        this.room = room;
        this.member = member;
        this.joinedAt = LocalDateTime.now();
        this.isHost = isHost != null ? isHost : false;
    }
}
