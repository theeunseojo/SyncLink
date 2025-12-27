package com.SyncLink.service;

import com.SyncLink.domain.Event;
import com.SyncLink.domain.IgnoredEvent;
import com.SyncLink.domain.Member;
import com.SyncLink.domain.Room;
import com.SyncLink.infrastructure.EventRepository;
import com.SyncLink.infrastructure.IgnoredEventRepository;
import com.SyncLink.infrastructure.MemberRepository;
import com.SyncLink.infrastructure.RoomMemberRepository;
import com.SyncLink.infrastructure.RoomRepository;
import com.SyncLink.presentation.EventResponseDto;
import com.SyncLink.presentation.TimeSlotDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final RoomMemberRepository roomMemberRepository;
    private final IgnoredEventRepository ignoredEventRepository;

    // 시작점과 종료지점을 기록할 수 있는 Point 클래스
    private static class Point {
        LocalDateTime time;
        int type; // 시작 +1, 종료 -1

        public Point(LocalDateTime time, int type) {
            this.time = time;
            this.type = type;
        }

        // 시간순으로 정렬하되, 같은 시간일 경우 시작지점이 먼저오게끔
        public static Comparator<Point> comparator() {
            return Comparator
                    .comparing((Point p) -> p.time)
                    .thenComparing(p -> p.type, Comparator.reverseOrder());
        }
    }

    // 무시하기로 한 일정을 제외하고 유효한 일정만 가져오기
    private List<Event> getValidEvents(Room room) {
        // RoomMember를 통해 방의 참여자들 조회
        List<Member> members = roomMemberRepository.findAllByRoom(room)
                .stream()
                .map(rm -> rm.getMember())
                .toList();
        List<Event> validEvents = new ArrayList<>();

        for (Member member : members) {
            List<Event> allEvents = eventRepository.findAllByMember(member);
            List<IgnoredEvent> ignoredList = ignoredEventRepository.findByRoomAndMember(room, member);

            // 필터링
            // eventId만 따로 뽑기
            List<String> ignoredIds = ignoredList.stream()
                    .map(igEvent -> igEvent.getGoogleEventId())
                    .toList();

            for (Event event : allEvents) {
                if (!ignoredIds.contains(event.getGoogleEventId())) {
                    validEvents.add(event);
                }
            }
        }

        return validEvents;
    }

    // 이벤트 리스트를 point리스트로 변환하는 함수
    private List<Point> splitEventByPoint(List<Event> events) {
        List<Point> points = new ArrayList<>();
        for (Event event : events) {
            // 시작시간
            points.add(new Point(event.getStartTime(), 1));
            // 끝시간
            points.add(new Point(event.getEndTime(), -1));
        }
        return points;
    }

    /*
     * 목적 : 일정들중에 빈시간을 찾는 알고리즘
     * - events : 구글 캘린더로 받아온 일정들
     * - rangeStart, rangeEnd : 방장이 설정한 범위 (시작시간, 종료시간)
     */
    private List<TimeSlotDto> findFreeTimes(List<Event> events, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        List<TimeSlotDto> freeTimes = new ArrayList<>();
        // 이벤트 -> point로 쪼개기
        List<Point> points = splitEventByPoint(events);
        // 정렬
        points.sort(Point.comparator());


        int count = 0;
        LocalDateTime lastFreeStart = rangeStart;
        System.out.println(lastFreeStart);
        for (Point point : points) {
            // 빈시간이 끝났으면
            // 그 이전까지의 빈시간을 기록
            if (count == 0 && point.type == 1) {
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
            if (count == 0) {
                lastFreeStart = point.time;
            }
        }
        // 개인 일정 순회 했음에도 불구하고 남은시간은 빈시간으로 기록
        if (lastFreeStart.isBefore(rangeEnd)) {
            LocalDateTime validStart = lastFreeStart.isBefore(rangeStart) ? rangeStart : lastFreeStart;
            if (validStart.isBefore(rangeEnd)) {
                freeTimes.add(new TimeSlotDto(validStart, rangeEnd));
            }
        }

        System.out.println("빈 일정의 갯수는 : " + freeTimes.size());
        return freeTimes;
    }

    // 방 찾아서 멤버들의 일정을 찾아서 빈시간 계산
    @Transactional(readOnly = true)
    public List<TimeSlotDto> getFreeTimesByRoom(String uuid, String sort) {

        // 방 찾기
        Room room = roomRepository.findByRoomUUID(uuid)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        List<Event> validEvents = getValidEvents(room);
        List<TimeSlotDto> results = findFreeTimes(validEvents, room.getStartTime(), room.getEndTime());

        // 내림차순
        if (sort.equals("LONGEST")) {
            results.sort((a, b) -> Long.compare(b.getDurationMin(), a.getDurationMin()));
        }

        return results;
    }

    // 일정 수정
    @Transactional
    public void toggleIgnoreEvent(String roomUuid, Long memberId, String googleEventId) {
        Room room = roomRepository.findByRoomUUID(roomUuid)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다ㅏ."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));

        // 이벤트 존재시 삭제, 없으면 다시 복구
        ignoredEventRepository.findByRoomAndMemberAndGoogleEventId(room, member, googleEventId)
                .ifPresentOrElse(
                        existing -> ignoredEventRepository.delete(existing),
                        () -> ignoredEventRepository.save(new IgnoredEvent(room, member, googleEventId)));

    }

    // 날짜로만 찾기
    @Transactional(readOnly = true)
    public List<LocalDate> findFreeDates(String roomUuid) {
        Room room = roomRepository.findByRoomUUID(roomUuid)
                .orElseThrow(() -> new IllegalArgumentException("방이 존재하지 않는다."));

        // 필터링된 이벤트 가져오기
        List<Event> events = getValidEvents(room);

        List<LocalDate> freeDates = new ArrayList<>();

        LocalDate current = room.getStartTime().toLocalDate();
        LocalDate end = room.getEndTime().toLocalDate();

        // 방의 시작일부터 종료일까지 하루씩 체크하기
        while (!current.isAfter(end)) {
            // 00:00으로 바꿔주기
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.plusDays(1).atStartOfDay();

            boolean isBusy = false;

            for (Event event : events) {
                // 일정이 겹치는지 계산
                // 일정시작 < 오늘 끝 && 일정 끝 > 오늘 시작
                if (event.getStartTime().isBefore(dayEnd) && event.getEndTime().isAfter(dayStart)) {
                    isBusy = true;
                    break;
                }
            }

            // 겹치는게 하나도 없으면 추가
            if (!isBusy) {
                freeDates.add(current);
            }
            current = current.plusDays(1);
        }

        return freeDates;
    }

    // 내 일정 목록과 함께 무시상태 조회
    @Transactional(readOnly = true)
    public List<EventResponseDto> getMemberEventsWithState(String uuid, Long memberId) {
        Room room = roomRepository.findByRoomUUID(uuid)
                .orElseThrow(() -> new IllegalArgumentException("방이 존재하지 않습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버가 존재하지 않습니다."));

        List<Event> allEvents = eventRepository.findAllByMember(member);
        List<String> ignoredIds = ignoredEventRepository.findByRoomAndMember(room, member)
                .stream()
                .map(igEvent -> igEvent.getGoogleEventId())
                .toList();

        return allEvents.stream()
                .map(event -> EventResponseDto.from(event, ignoredIds.contains(event.getGoogleEventId()) // true -> 무시된
                                                                                                         // 상태
                ))
                .toList();
    }

}
