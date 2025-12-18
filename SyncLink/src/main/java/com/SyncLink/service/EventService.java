package com.SyncLink.service;


import com.SyncLink.domain.Member;
import com.SyncLink.infrastructure.EventRepository;
import com.SyncLink.infrastructure.MemberRepository;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final MemberRepository memberRepository;
    private  final EventRepository eventRepository;

    // 기본 설정
    private static final String APPLICATION_NAME = "SyncLink";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    // 날짜 변환 LocalDateTime -> DateTime
    private DateTime toGoogleDateTime(LocalDateTime localDateTime){
        if(localDateTime == null) return null;

        long epochMills = localDateTime.atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        return new DateTime(epochMills);
    }

    //날짜 변환 DateTime -> LocalDateTime
    private LocalDateTime toLocalDateTime(DateTime googleTime){
        if(googleTime == null) return null;
        long epochMillis = googleTime.getValue();

        // 종일일정 -> 한국시간대로 나오는 경우 고려해서 형식을 00:00:00으로 맞추기
        if (googleTime.isDateOnly()) {
            return LocalDate.parse(googleTime.toStringRfc3339()).atStartOfDay();
        }

        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(epochMillis),
                ZoneId.systemDefault()
        );
    }


    @Transactional
    public void saveEvents(String email) throws Exception{
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        // 캘린더 요청할 토큰을 찾기 위해 멤버를 찾는다.
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("member 객체가 존재하지 않음"));

        // 조회 범위 설정 (오늘부터 약 3개월정도 가져오기)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusMonths(3);
        DateTime timeMin = toGoogleDateTime(now);
        DateTime timeMax = toGoogleDateTime(future);

        // 요청 준비
        Calendar service = new Calendar.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                request -> request.getHeaders().setAuthorization("Bearer " + member.getToken())
        ).setApplicationName(APPLICATION_NAME).build();

        // 요청
        Events events = service.events().list("primary")
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .execute();

        //저장
        // 기존의 데이터가 있다면 삭제하고 다시 저장하기
        // 각 이벤트마다 찢어서 저장하기
        List<Event> items = events.getItems();

        if(!items.isEmpty()){

            // 일정들을 일정별로 쪼개서 저장하기
            for(Event event : items){

                // 시간 가져오기
                DateTime start = event.getStart().getDateTime();
                DateTime end = event.getEnd().getDateTime();

                // 날짜만 존재할 경우
                if(start == null){
                    start = event.getStart().getDate();
                }
                if(end ==  null){
                    end = event.getEnd().getDate();
                }


                // 시간 변환
                LocalDateTime startLocal = toLocalDateTime(start);
                LocalDateTime endLocal = toLocalDateTime(end);


                // 부분 업데이트
                eventRepository.findByGoogleEventId(event.getId())
                        .ifPresentOrElse(
                                existingEvent -> {
                                    existingEvent.setTitle(event.getSummary());
                                    existingEvent.setStartTime(startLocal);
                                    existingEvent.setEndTime(endLocal);
                                },
                                () -> {
                                    // Event 도메인으로 변환
                                    com.SyncLink.domain.Event newEvent = com.SyncLink.domain.Event.builder()
                                            .member(member)
                                            .googleEventId(event.getId()) // 구글 ID 필수
                                            .title(event.getSummary())
                                            .startTime(startLocal)
                                            .endTime(endLocal)
                                            .build();
                                    eventRepository.save(newEvent);

                                }
                        );

                // 확인용
                System.out.printf("%s (%s)\n", event.getSummary(), start);

            }

        }



    }


}
