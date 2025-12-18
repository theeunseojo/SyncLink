package com.SyncLink.presentation;


import java.time.LocalDateTime;

public record RoomCreateRequest(String title,LocalDateTime startDateTime,
      LocalDateTime endDateTime) {
}
