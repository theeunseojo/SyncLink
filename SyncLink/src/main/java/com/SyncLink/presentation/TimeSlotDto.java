package com.SyncLink.presentation;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 빈 시간대 정보를 담는 DTO.
 */
public record TimeSlotDto(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime start,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime end,

        List<MemberDto> availableMembers,

        int votes) {
    /**
     * 기존 호환용 생성자 (빈 멤버 목록, 투표 0)
     */
    public TimeSlotDto(LocalDateTime start, LocalDateTime end) {
        this(start, end, List.of(), 0);
    }

    /**
     * 시간대의 총 분 단위 길이를 계산합니다.
     */
    public Long getDurationMin() {
        return Duration.between(start, end).toMinutes();
    }

    /**
     * 참여 가능한 멤버 수를 반환합니다.
     */
    public int getAvailableCount() {
        return availableMembers != null ? availableMembers.size() : 0;
    }
}
