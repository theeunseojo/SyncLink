package com.SyncLink.auth;


import com.SyncLink.domain.Member;
import com.SyncLink.infrastructure.MemberRepository;
import com.SyncLink.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final EventService eventService;
    private final MemberRepository memberRepository;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
       // 이벤트
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email").toString();

        try{
            eventService.saveEvents(email);
            System.out.println("로그인 성공: 일정 동기화 완료 (" + email + ")");
        }catch (Exception e){
            e.getMessage();
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일에 해당하는 회원이 없습니다."));

        HttpSession session = request.getSession();
        session.setAttribute("memberId", member.getId());

        // 리다이렉트 로직
        Object redirectUuidObj = session.getAttribute("redirectUuid");

        // 방 정보 존재시 방으로 리다이렉트
        if (redirectUuidObj != null) {
            String redirectUuid = redirectUuidObj.toString();
            session.removeAttribute("redirectUuid");
            getRedirectStrategy().sendRedirect(request, response, "/room.html?uuid=" + redirectUuid + "&login=success");
        } else {
            // 방 정보 존재 X -> 메인페이지
            getRedirectStrategy().sendRedirect(request, response, "/index.html?login=success");
        }
    }
}
