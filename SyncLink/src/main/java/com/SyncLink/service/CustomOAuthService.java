package com.SyncLink.service;

import com.SyncLink.domain.Member;
import com.SyncLink.domain.Room;
import com.SyncLink.enums.ServiceType;
import com.SyncLink.infrastructure.MemberRepository;
import com.SyncLink.infrastructure.RoomRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * OAuth2 로그인 처리 서비스.
 * 사용자 정보 조회/생성 및 방 참여 처리를 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuthService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final RoomService roomService;
    private final HttpSession session;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(req);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String accessToken = req.getAccessToken().getTokenValue();
        ServiceType serviceType = determineServiceType(req.getClientRegistration().getRegistrationId());

        Member member = findOrCreateMember(email, name, accessToken, serviceType);
        memberRepository.save(member);

        joinRoomIfRequired(member);

        log.info("OAuth 로그인 성공: {}", name);

        return oAuth2User;
    }

    /**
     * 기존 멤버를 찾거나 새로 생성합니다.
     */
    private Member findOrCreateMember(String email, String name, String accessToken, ServiceType serviceType) {
        return memberRepository.findByEmail(email)
                .map(existing -> existing.updateToken(accessToken))
                .orElseGet(() -> Member.builder()
                        .email(email)
                        .name(name)
                        .token(accessToken)
                        .serviceType(serviceType)
                        .build());
    }

    /**
     * 세션에 저장된 방 UUID가 있으면 해당 방에 참여합니다.
     */
    private void joinRoomIfRequired(Member member) {
        Object uuidObj = session.getAttribute("redirectUuid");
        if (uuidObj == null) {
            return;
        }

        String redirectUuid = uuidObj.toString();
        Room room = roomRepository.findByRoomUUID(redirectUuid).orElse(null);

        if (room != null) {
            roomService.joinRoom(redirectUuid, member);
            log.debug("방 참여 완료: {}", redirectUuid);
        }
    }

    /**
     * OAuth Provider ID를 ServiceType으로 변환합니다.
     */
    private ServiceType determineServiceType(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> ServiceType.GOOGLE;
            case "naver" -> ServiceType.NAVER;
            case "apple" -> ServiceType.APPLE;
            default -> ServiceType.NONE;
        };
    }
}
