package com.SyncLink.presentation;

import com.SyncLink.service.RoomService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @PostMapping("/api/rooms")
    public ResponseEntity<String> createRoom(@RequestBody RoomCreateRequest request, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");

        if (memberId == null) {
            return ResponseEntity.status(401).build();
        }

        String uuid = roomService.createRoom(request, memberId);
        return ResponseEntity.ok(uuid);
    }

    @GetMapping("/api/rooms/{uuid}/members")
    public ResponseEntity<List<String>> getRoomMembers(@PathVariable String uuid) {
        List<String> memberNames = roomService.getMemberByRoom(uuid);
        return ResponseEntity.ok(memberNames);
    }
}
