package com.SyncLink.presentation;

import com.SyncLink.domain.Member;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public record TimeSlotDto(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime start,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime end,
        List<memberDto> availableMembers,
        int votes
        ){
    public Long getDurationMin() {
        return Duration.between(start, end).toMinutes();
    }
    public int getAvailableCount(){
        return availableMembers.size();
    }
}
