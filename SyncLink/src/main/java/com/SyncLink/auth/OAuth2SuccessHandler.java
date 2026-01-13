package com.SyncLink.auth;

import com.SyncLink.domain.Member;
import com.SyncLink.infrastructure.MemberRepository;
import com.SyncLink.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OAuth2 인증 성공 핸들러.
 * 로그인 성공 후 일정 동기화 및 적절한 페이지로 리다이렉트합니다.
 */
@Slf4j
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

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일에 해당하는 회원이 없습니다: " + email));

        HttpSession session = request.getSession();
        session.setAttribute("memberId", member.getId());

        syncEvents(member);

        String redirectUrl = determineRedirectUrl(request, session);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    /**
     * 멤버의 캘린더 일정을 동기화합니다.
     */
    private void syncEvents(Member member) {
        try {
            eventService.saveEvents(member.getId());
            log.info("일정 동기화 완료: {}", member.getEmail());
        } catch (Exception e) {
            log.warn("일정 동기화 실패 ({}): {}", member.getEmail(), e.getMessage());
        }
    }

    /**
     * 리다이렉트 URL을 결정합니다.
     * 우선순위: 세션 저장 UUID > SavedRequest > Referer > 기본값
     */
    private String determineRedirectUrl(HttpServletRequest request, HttpSession session) {
        // 1. 세션에 저장된 redirectUuid 확인
        String redirectUrl = getRedirectFromSession(session);
        if (redirectUrl != null) {
            return redirectUrl;
        }

        // 2. SavedRequest 확인 (Spring Security가 저장한 원래 요청)
        redirectUrl = getRedirectFromSavedRequest(request);
        if (redirectUrl != null) {
            return redirectUrl;
        }

        // 3. Referer 헤더 확인
        redirectUrl = getRedirectFromReferer(request);
        if (redirectUrl != null) {
            return redirectUrl;
        }

        // 4. 기본값: 메인 페이지
        return "/index.html?login=success";
    }

    private String getRedirectFromSession(HttpSession session) {
        Object redirectUuidObj = session.getAttribute("redirectUuid");
        if (redirectUuidObj == null) {
            return null;
        }

        String redirectUuid = redirectUuidObj.toString();
        session.removeAttribute("redirectUuid");
        return "/room.html?uuid=" + redirectUuid + "&login=success";
    }

    private String getRedirectFromSavedRequest(HttpServletRequest request) {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        SavedRequest savedRequest = requestCache.getRequest(request, null);

        if (savedRequest == null) {
            return null;
        }

        String targetUrl = savedRequest.getRedirectUrl();
        if (targetUrl != null && targetUrl.contains("room.html")) {
            String separator = targetUrl.contains("?") ? "&" : "?";
            return targetUrl + separator + "login=success";
        }

        return null;
    }

    private String getRedirectFromReferer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || !referer.contains("room.html")) {
            return null;
        }

        String uuid = extractUuidFromUrl(referer);
        if (uuid != null) {
            return "/room.html?uuid=" + uuid + "&login=success";
        }

        return null;
    }

    /**
     * URL에서 uuid 파라미터 값을 추출합니다.
     */
    private String extractUuidFromUrl(String url) {
        if (!url.contains("uuid=")) {
            return null;
        }

        int start = url.indexOf("uuid=") + 5;
        int end = url.indexOf("&", start);
        return end > 0 ? url.substring(start, end) : url.substring(start);
    }
}
