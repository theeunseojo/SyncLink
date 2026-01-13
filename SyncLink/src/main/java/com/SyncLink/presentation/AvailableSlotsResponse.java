package com.SyncLink.presentation;

import java.util.List;

/**
 * 가용 시간대 응답 DTO.
 */
public record AvailableSlotsResponse(
                List<TimeSlotDto> slots,
                List<MemberDto> members) {
}
