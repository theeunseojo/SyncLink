package com.SyncLink.presentation;


import com.SyncLink.service.ScheduleService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

//    @GetMapping("/api/schedule/{uuid}/events")
//    public ResponseEntity<?> scheduleEventsByRoom(@PathVariable String uuid){
//        List<TimeSlotDto> events = scheduleService.getFreeTimesByRoom(uuid);
//        return ResponseEntity.ok(events);
//    }

    // 시간모드 ( 빈 시간 찾기)
    @GetMapping("/api/rooms/{uuid}/available-times")
    public ResponseEntity<List<TimeSlotDto>> getAvailableTimes(@PathVariable String uuid) {
        List<TimeSlotDto> slots = scheduleService.getFreeTimesByRoom(uuid);
        return ResponseEntity.ok(slots);
    }

    // 날짜모드
    @GetMapping("/api/rooms/{uuid}/available-dates")
    public ResponseEntity<List<LocalDate>> getAvailableDates(@PathVariable String uuid){
        List<LocalDate> dates = scheduleService.findFreeDates(uuid);
        return ResponseEntity.ok(dates);
    }

    // 모든 일정 조회
    @GetMapping("/api/rooms/{uuid}/my-events")
    public ResponseEntity<List<EventResponseDto>> getMyEvents(@PathVariable String uuid, HttpSession session){
        Long memberId = (Long) session.getAttribute("memberId");

        // 비로그인 상태시 401 에러
        if(memberId == null){
            return ResponseEntity.status(401).build();
        }

        List<EventResponseDto> events = scheduleService.getMemberEventsWithState(uuid, memberId);
        return ResponseEntity.ok(events);
    }

    // 일정 무시
    @PostMapping("/api/rooms/{uuid}/events/{eventId}")
    public ResponseEntity<Void> toggleIgnore(@PathVariable String uuid, @PathVariable String eventId, HttpSession session){
        Long memberId = (Long) session.getAttribute("memberId");

        if(memberId == null){
            return ResponseEntity.status(401).build();
        }

        scheduleService.toggleIgnoreEvent(uuid, memberId, eventId);

        return ResponseEntity.ok().build();
    }



}
