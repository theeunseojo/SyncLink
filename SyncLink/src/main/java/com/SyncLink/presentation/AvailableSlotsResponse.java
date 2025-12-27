package com.SyncLink.presentation;

import java.util.List;

public record AvailableSlotsResponse(
        List<TimeSlotDto> slots,
        List<memberDto> members
) {
}
