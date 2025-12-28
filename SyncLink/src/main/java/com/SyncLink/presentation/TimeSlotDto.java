package com.SyncLink.presentation;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public record TimeSlotDto(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime start,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime end,
        List<memberDto> availableMembers,
        int votes) {
    // 기존 호환용 생성자 (빈 멤버, 투표 0)
    public TimeSlotDto(LocalDateTime start, LocalDateTime end) {
        this(start, end, List.of(), 0);
    }

    public Long getDurationMin() {
        return Duration.between(start, end).toMinutes();
    }

    public int getAvailableCount() {
        return availableMembers != null ? availableMembers.size() : 0;
    }
}
