package com.SyncLink.presentation;

import com.SyncLink.enums.roomMode;

import java.time.LocalDateTime;

public record RoomCreateRequest(
        String title,
        roomMode mode,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime) {
}
