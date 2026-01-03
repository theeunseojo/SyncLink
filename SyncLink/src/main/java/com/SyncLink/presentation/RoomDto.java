package com.SyncLink.presentation;

import com.SyncLink.enums.roomMode;

import java.time.LocalDateTime;
import java.util.List;

public record RoomDto(
        String roomUUId,
        String title,
        roomMode mode,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Long hostId,
        List<memberDto> members,
        ConfirmedSlotDto confirmedSlot
) {
}
