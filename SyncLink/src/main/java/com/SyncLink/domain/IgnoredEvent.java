package com.SyncLink.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class IgnoredEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id") // DB 컬럼명은 room_id가 됨
    private Room room;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id") // DB 컬럼명은 member_id가 됨
    private Member member;


    private String externalEventId;


    public IgnoredEvent(Room room, Member member, String externalEventId) {
        this.room = room;
        this.member = member;
        this.externalEventId = externalEventId;
    }
}
