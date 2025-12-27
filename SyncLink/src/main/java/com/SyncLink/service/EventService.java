package com.SyncLink.service;

import com.SyncLink.domain.Member;
import com.SyncLink.infrastructure.MemberRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {
    private final MemberRepository memberRepository;


    @Transactional
    public void saveEvents(Long memberId) throws Exception {

        // 캘린더 요청할 토큰을 찾기 위해 멤버를 찾는다.
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("member 객체가 존재하지 않음"));



    }

}
