package com.SyncLink.presentation;

import com.SyncLink.service.EventService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    // 캘린더데이터 저장하기
    @PostMapping("/api/events")
    public ResponseEntity<String> createEvents(HttpSession session) throws Exception {
        Long memberId = (Long) session.getAttribute("memberId");

        if (memberId == null) {
            return ResponseEntity.status(401).build();
        }

        eventService.saveEvents(memberId);
        return ResponseEntity.ok("캘린더 불러오기 성공");
    }
}
