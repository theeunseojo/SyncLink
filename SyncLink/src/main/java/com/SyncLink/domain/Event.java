package com.SyncLink.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String googleEventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Builder
    public Event(Member member, String title, LocalDateTime startTime, LocalDateTime endTime, String googleEventId) {
        this.member = member;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.googleEventId= googleEventId;
    }

}
