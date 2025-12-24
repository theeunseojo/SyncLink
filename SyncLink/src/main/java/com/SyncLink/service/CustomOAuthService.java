package com.SyncLink.service;

import com.SyncLink.domain.Member;
import com.SyncLink.domain.Room;
import com.SyncLink.infrastructure.MemberRepository;
import com.SyncLink.infrastructure.RoomRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuthService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final RoomService roomService;
    private final HttpSession session;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
        OAuth2User auth2User = super.loadUser(req);
        String email = auth2User.getAttribute("email");
        String name = auth2User.getAttribute("name");
        String accessToken = req.getAccessToken().getTokenValue();

        // 세션에서 redirectUuid 꺼내기
        Object uuidObj = session.getAttribute("redirectUuid");
        String redirectUuid = (uuidObj != null) ? uuidObj.toString() : null;

        // 멤버 조회/생성 및 토큰 갱신
        Member member = memberRepository.findByEmail(email)
                .map(mem -> mem.updateToken(accessToken))
                .orElseGet(() -> Member.builder()
                        .email(email)
                        .name(name)
                        .token(accessToken)
                        .build());

        memberRepository.save(member);

        // 방이 존재하면 RoomService를 통해 참여
        if (redirectUuid != null) {
            Room room = roomRepository.findByRoomUUID(redirectUuid).orElse(null);
            if (room != null) {
                roomService.joinRoom(redirectUuid, member);
            }
        }

        System.out.println("멤버 저장완료: " + name);

        return auth2User;
    }
}
