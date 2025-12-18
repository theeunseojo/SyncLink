package com.SyncLink.presentation;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login/google")
    public String loginWithRedirect(@RequestParam(required = false) String uuid, HttpSession session) {

        if (uuid != null) {
            // 세션(서버 메모리)에 방 번호
            session.setAttribute("redirectUuid", uuid);
        }

        // 그러고 나서 구글 로그인 페이지로
        return "redirect:/oauth2/authorization/google";
    }
}
