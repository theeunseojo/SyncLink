package com.SyncLink.service;


import com.SyncLink.domain.Event;
import com.SyncLink.domain.Member;
import com.SyncLink.domain.Room;
import com.SyncLink.infrastructure.EventRepository;
import com.SyncLink.infrastructure.MemberRepository;
import com.SyncLink.infrastructure.RoomRepository;
import com.SyncLink.presentation.TimeSlotDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final EventRepository eventRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;

    // 시작점과 종료지점을 기록할 수 있는 Point 클래스
    private static class Point{
        LocalDateTime time;
        int type;   // 시작 +1, 종료 -1

        public Point(LocalDateTime time, int type){
            this.time = time;
            this.type = type;
        }

        // 시간순으로 정렬하되, 같은 시간일 경우 시작지점이 먼저오게끔
        public static Comparator<Point> comparator(){
            return Comparator
                    .comparing((Point p) -> p.time)
                    .thenComparing(p -> p.type, Comparator.reverseOrder());
        }
    }



    // 이벤트 리스트를 point리스트로 변환하는 함수
    private List<Point> splitEventByPoint(List<Event> events){
        List<Point> points = new ArrayList<>();
        for(Event event : events){
            // 시작시간
            points.add(new Point(event.getStartTime(), 1));
            // 끝시간
            points.add(new Point(event.getEndTime(), -1));
        }
        return points;
    }

    /*
    목적 : 일정들중에 빈시간을 찾는 알고리즘
    - events : 구글 캘린더로 받아온 일정들
    - rangeStart, rangeEnd : 방장이 설정한 범위 (시작시간, 종료시간)
    */
    private List<TimeSlotDto> findFreeTimes(List<Event> events, LocalDateTime rangeStart, LocalDateTime rangeEnd){
        List<TimeSlotDto> freeTimes = new ArrayList<>();
        // 이벤트 -> point로 쪼개기
        List<Point> points= splitEventByPoint(events);
        // 정렬
        points.sort(Point.comparator());

        int count = 0;
        LocalDateTime lastFreeStart = rangeStart;
        System.out.println(lastFreeStart);
        for(Point point : points){
            // 빈시간이 끝났으면
            // 그 이전까지의 빈시간을 기록
            if(count == 0 && point.type == 1){
                // 빈시간이 시작시간이 방장 범위 설정 시작 시간(= 앞서있으면 시작시간은 rangeStart
                // 끝시간이 방정 범위 설정 종료 시간보다 뒤에 있으면 끝 시간은 rangeEnd
                LocalDateTime validStart = lastFreeStart.isBefore(rangeStart) ? rangeStart : lastFreeStart;
                LocalDateTime validEnd = point.time.isAfter(rangeEnd) ? rangeEnd : point.time;

                // 시작시간 = 종료시간이 같은지 체크
                if (validStart.isBefore(validEnd)) {
                    freeTimes.add(new TimeSlotDto(validStart, validEnd));
                }

            }
            // 카운팅
            // 시작시간이면 +1
            // 종료시간이면 -1
            count += point.type;

            // 빈시간이 시작되면 이전의 종료시간을 기록
            if(count == 0){
                lastFreeStart = point.time;
            }
        }
        // 개인 일정 순회 했음에도 불구하고 남은시간은 빈시간으로 기록
        if(lastFreeStart.isBefore(rangeEnd)){
            LocalDateTime validStart = lastFreeStart.isBefore(rangeStart) ? rangeStart : lastFreeStart;
            if (validStart.isBefore(rangeEnd)) {
                freeTimes.add(new TimeSlotDto(validStart, rangeEnd));
            }
        }

        System.out.println("일정의 갯수는 : " + freeTimes.size());
        return freeTimes;
    }

    // 방 찾아서 멤버들의 일정을 찾아서 빈시간 계산
    @Transactional(readOnly = true)
    public List<TimeSlotDto> getFreeTimesByRoom(String uuid){

        // 방 찾기
        Room room = roomRepository.findByRoomUUID(uuid)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        // 방에 존재하는 모든 멤버 일정 가져오기
        List<Member> members = memberRepository.findAllByRoom(room);

        // 일정 모으기
        List<Event> events = new ArrayList<>();
        for(Member member : members){
            events.addAll(eventRepository.findAllByMember(member));
        }


        return findFreeTimes(events,room.getStartTime(), room.getEndTime());
    }
}
