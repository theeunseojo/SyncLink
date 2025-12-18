package com.SyncLink.presentation;

import com.SyncLink.domain.Event;

import java.time.LocalDateTime;


public record EventResponseDto(String id, String title, LocalDateTime start, LocalDateTime end, boolean isIgnored){

    // 엔티티 -> dto
    public static EventResponseDto from(Event event, boolean isIgnored){
        return new EventResponseDto(
                event.getGoogleEventId(),
                event.getTitle(),
                event.getStartTime(),
                event.getEndTime(),
                isIgnored
        );
    }
}
