package com.SyncLink.presentation;


import com.SyncLink.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    @GetMapping("/api/schedule/{uuid}/events")
    public ResponseEntity<?> scheduleEventsByRoom(@PathVariable String uuid){
        List<TimeSlotDto> events = scheduleService.getFreeTimesByRoom(uuid);
        return ResponseEntity.ok(events);
    }
}
