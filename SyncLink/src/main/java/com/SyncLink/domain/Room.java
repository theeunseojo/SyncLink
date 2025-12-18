package com.SyncLink.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomUUID;
    private String title;

    // 범위
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Builder
    public Room(String roomUUID, String title, LocalDateTime startTime, LocalDateTime endTime){
        this.roomUUID = roomUUID;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }

}
