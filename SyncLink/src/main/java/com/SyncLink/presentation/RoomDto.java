package com.SyncLink.presentation;

import com.SyncLink.enums.RoomMode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 방 정보를 담는 DTO.
 */
public record RoomDto(
                String roomUUId,
                String title,
                RoomMode mode,
                LocalDateTime startDateTime,
                LocalDateTime endDateTime,
                Long hostId,
                List<MemberDto> members,
                ConfirmedSlotDto confirmedSlot) {
}
