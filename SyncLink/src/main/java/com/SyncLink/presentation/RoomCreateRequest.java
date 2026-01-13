package com.SyncLink.presentation;

import com.SyncLink.enums.RoomMode;

import java.time.LocalDateTime;

/**
 * 방 생성 요청 DTO.
 */
public record RoomCreateRequest(
                String title,
                RoomMode mode,
                LocalDateTime startDateTime,
                LocalDateTime endDateTime) {
}
