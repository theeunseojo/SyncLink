package com.SyncLink.adapter;

import com.SyncLink.domain.Event;
import com.SyncLink.domain.Member;
import com.SyncLink.enums.ServiceType;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;


@Component
public class GoogleCalendarAdapter implements CalendarAdapter{


    // 날짜 변환 LocalDateTime -> DateTime
    private DateTime toGoogleDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null)
            return null;

        long epochMills = localDateTime.atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        return new DateTime(epochMills);
    }

    // 날짜 변환 DateTime -> LocalDateTime
    private LocalDateTime toLocalDateTime(DateTime googleTime) {
        if (googleTime == null)
            return null;
        long epochMillis = googleTime.getValue();

        // 종일일정 -> 한국시간대로 나오는 경우 고려해서 형식을 00:00:00으로 맞추기
        if (googleTime.isDateOnly()) {
            return LocalDate.parse(googleTime.toStringRfc3339()).atStartOfDay();
        }

        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(epochMillis),
                ZoneId.systemDefault());
    }

    // 기본 설정
    private static final String APPLICATION_NAME = "SyncLink";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();



    @Override
    public List<Event> fetchEvents(Member member, LocalDateTime start, LocalDateTime end) throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        // 조회 범위 설정
        DateTime timeMin = toGoogleDateTime(start);
        DateTime timeMax = toGoogleDateTime(end);

        // 구글 API 호출
        // 요청 준비
        Calendar service = new Calendar.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                request -> request.getHeaders().setAuthorization("Bearer " + member.getToken()))
                .setApplicationName(APPLICATION_NAME).build();

        // 요청
        Events events = service.events().list("primary")
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .execute();

        // google Event -> domain Event 변경
        List<com.google.api.services.calendar.model.Event> items = events.getItems();
        List<Event> eventList = new ArrayList<>();

        if (!items.isEmpty()) {
            // 일정 순회
            for (com.google.api.services.calendar.model.Event googleEvent : items) {

                // 시간 가져오기
                DateTime st = googleEvent.getStart().getDateTime();
                DateTime ed = googleEvent.getEnd().getDateTime();

                // 날짜만 존재할 경우
                if (st == null) {
                    st = googleEvent.getStart().getDate();
                }
                if (ed == null) {
                    ed = googleEvent.getEnd().getDate();
                }

                // 시간 변환
                LocalDateTime startLocal = toLocalDateTime(st);
                LocalDateTime endLocal = toLocalDateTime(ed);


                // 도메인 event 생성
                Event domainEvent = Event.builder()
                        .externalId(googleEvent.getId())
                        .title(googleEvent.getSummary())
                        .member(member)
                        .startTime(startLocal)
                        .endTime(endLocal)
                        .build();


                eventList.add(domainEvent);
            }
        }

        // 리스트 반환
        return eventList;
    }

    @Override
    public boolean supports(ServiceType type) {
        return type == ServiceType.GOOGLE;
    }
}
