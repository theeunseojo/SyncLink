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
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final EventService eventService;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email").toString();

        // 먼저 member를 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일에 해당하는 회원이 없습니다."));

        // 세션에 memberId 저장
        HttpSession session = request.getSession();
        session.setAttribute("memberId", member.getId());

        // 이벤트 동기화 (memberId 사용)
        try {
            eventService.saveEvents(member.getId());
            System.out.println("로그인 성공: 일정 동기화 완료 (" + email + ")");
        } catch (Exception e) {
            System.out.println("일정 동기화 실패: " + e.getMessage());
        }

        // 리다이렉트 결정
        String redirectUrl = determineRedirectUrl(request, session);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String determineRedirectUrl(HttpServletRequest request, HttpSession session) {
        // 1. 세션에 저장된 redirectUuid 확인
        Object redirectUuidObj = session.getAttribute("redirectUuid");
        if (redirectUuidObj != null) {
            String redirectUuid = redirectUuidObj.toString();
            session.removeAttribute("redirectUuid");
            return "/room.html?uuid=" + redirectUuid + "&login=success";
        }

        // 2. SavedRequest 확인 (Spring Security가 저장한 원래 요청)
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        SavedRequest savedRequest = requestCache.getRequest(request, null);
        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();
            if (targetUrl != null && targetUrl.contains("room.html")) {
                return targetUrl + (targetUrl.contains("?") ? "&" : "?") + "login=success";
            }
        }

        // 3. Referer 헤더 확인
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("room.html")) {
            // room.html?uuid=xxx 형식에서 uuid 추출
            if (referer.contains("uuid=")) {
                int start = referer.indexOf("uuid=") + 5;
                int end = referer.indexOf("&", start);
                String uuid = end > 0 ? referer.substring(start, end) : referer.substring(start);
                return "/room.html?uuid=" + uuid + "&login=success";
            }
        }

        // 4. 기본값: 메인 페이지
        return "/index.html?login=success";
    }
}
