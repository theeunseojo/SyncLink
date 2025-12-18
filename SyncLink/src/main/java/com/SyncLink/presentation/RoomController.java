package com.SyncLink.presentation;



import com.SyncLink.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;


    @PostMapping("/api/rooms")
    public ResponseEntity<String> createRoom(@RequestBody RoomCreateRequest request, @AuthenticationPrincipal OAuth2User user){
        // 이메일
        String email = user.getAttribute("email");

        String uuid = roomService.createRoom(request,email);
        return ResponseEntity.ok(uuid);
    }

    @GetMapping("/api/rooms/{uuid}/members")
    public ResponseEntity<List<String>> getRoomMembers(@PathVariable String uuid){
        List<String> memberNames = roomService.getMemberByRoom(uuid);
        return ResponseEntity.ok(memberNames);
    }


}
