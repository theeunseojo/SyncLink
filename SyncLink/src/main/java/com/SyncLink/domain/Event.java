package com.SyncLink.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Builder
    public Event(Member member, String title, LocalDateTime startTime, LocalDateTime endTime) {
        this.member = member;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }

}
