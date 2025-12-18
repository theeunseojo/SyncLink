package com.SyncLink.service;


import com.SyncLink.domain.Member;
import com.SyncLink.domain.Room;
import com.SyncLink.infrastructure.EventRepository;
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
    private final EventRepository eventRepository;
    private final HttpSession session;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException{
        OAuth2User  auth2User = super.loadUser(req);
        String email = auth2User.getAttribute("email");
        String name = auth2User.getAttribute("name");
        String accessToken = req.getAccessToken().getTokenValue();

        // 1. 값을 안전하게 꺼냅니다. (Object로 받기)
        Object uuidObj = session.getAttribute("redirectUuid");

        // 2. 값이 있으면 String으로 바꾸고, 없으면 null로
        // 처음 세션에는 저장되어있지 않음
        String redirectUuid = (uuidObj != null) ? uuidObj.toString() : null;

        Room room = null;
        // 방이 존재할시
        if(redirectUuid != null){
            room = roomRepository.findByRoomUUID(redirectUuid)
                    .orElse(null);
        }
        // 레포지토리에 저장
        // 멤버가 레포지토리에 저장되어있으면 토큰만 갱신
        // 멤버가 저장되어있지 않으면 회원가입
        Member member = memberRepository.findByEmail(email)
                        .map(mem -> mem.updateToken(accessToken))
                                .orElseGet(() -> Member.builder()
                                        .email(email)
                                        .name(name)
                                        .token(accessToken)
                                        .build());
        if(room != null){
            member.setRoom(room);
        }
        memberRepository.save(member);
        System.out.println("멤버 저장완료" + name);

        return auth2User;
    }


}
