package com.SyncLink.domain;

import com.SyncLink.enums.roomMode;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomUUID;
    private String title;

    // 범위
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private roomMode mode; // DATE_TIME / DATE_ONLY
    private Long hostId; // 방장 ID

    private LocalDateTime confirmedStart; // 확정된 시작 시간 (nullable)
    private LocalDateTime confirmedEnd; // 확정된 종료 시간 (nullable)

    @Builder
    public Room(String roomUUID, String title, LocalDateTime startTime, LocalDateTime endTime) {
        this.roomUUID = roomUUID;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }

}
