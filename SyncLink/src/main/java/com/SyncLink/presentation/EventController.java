package com.SyncLink.presentation;


import com.SyncLink.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    // 캘린더데이터 저장하기
    @PostMapping("/api/events")
    public ResponseEntity<String> createEvents(@AuthenticationPrincipal OAuth2User oAuth2User) throws Exception{
        String email = oAuth2User.getAttribute("email");
        eventService.saveEvents(email);
        return ResponseEntity.ok("캘린더 불러오기 성공");
    }
}
