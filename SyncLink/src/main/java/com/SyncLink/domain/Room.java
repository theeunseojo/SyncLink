package com.SyncLink.domain;

import com.SyncLink.enums.RoomMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 일정 조율을 위한 방 엔티티.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomUUID;
    private String title;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private RoomMode mode;

    private Long hostId;

    private LocalDateTime confirmedStart;
    private LocalDateTime confirmedEnd;

    @Builder
    public Room(String roomUUID, String title, LocalDateTime startTime, LocalDateTime endTime) {
        this.roomUUID = roomUUID;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
