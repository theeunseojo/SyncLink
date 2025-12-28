package com.SyncLink.service;

import com.SyncLink.adapter.CalendarAdapter;
import com.SyncLink.adapter.CalendarAdapterFactory;
import com.SyncLink.domain.Event;
import com.SyncLink.domain.Member;
import com.SyncLink.enums.ServiceType;
import com.SyncLink.infrastructure.MemberRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final MemberRepository memberRepository;
    private final CalendarAdapterFactory calendarAdapterFactory;

    @Transactional
    public void saveEvents(Long memberId) throws Exception{

        // 캘린더 요청할 토큰을 찾기 위해 멤버를 찾는다.
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("member 객체가 존재하지 않음"));

        ServiceType type = member.getServiceType();
        // 원하는 서비스를 가지고 온다.
        CalendarAdapter adapter = calendarAdapterFactory.getAdapter(type);

        // domain Event로 가져오기
        List<Event> events = adapter.fetchEvents(member, LocalDateTime.now(), LocalDateTime.now().plusMonths(3));

        // 저장하기

    }

}
