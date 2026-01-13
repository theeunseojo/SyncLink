package com.SyncLink.service;

import com.SyncLink.domain.Event;
import com.SyncLink.domain.IgnoredEvent;
import com.SyncLink.domain.Member;
import com.SyncLink.domain.Room;
import com.SyncLink.domain.RoomMember;
import com.SyncLink.domain.TimePoint;
import com.SyncLink.enums.MemberType;
import com.SyncLink.infrastructure.EventRepository;
import com.SyncLink.infrastructure.IgnoredEventRepository;
import com.SyncLink.infrastructure.MemberRepository;
import com.SyncLink.infrastructure.RoomMemberRepository;
import com.SyncLink.infrastructure.RoomRepository;
import com.SyncLink.presentation.EventResponseDto;
import com.SyncLink.presentation.MemberDto;
import com.SyncLink.presentation.TimeSlotDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 일정 관련 비즈니스 로직을 담당하는 서비스.
 * 빈 시간 계산, 일정 무시 토글, 멤버별 일정 조회 등을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final EventRepository eventRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final IgnoredEventRepository ignoredEventRepository;

    // ==================== 공개 API ====================

    /**
     * 특정 방의 빈 시간대를 조회합니다.
     * 각 시간대에 참여 가능한 멤버 정보도 함께 반환합니다.
     */
    @Transactional(readOnly = true)
    public List<TimeSlotDto> getFreeTimesByRoom(String uuid, String sort) {
        Room room = findRoomByUuid(uuid);
        List<Event> validEvents = getValidEvents(room);
        List<TimeSlotDto> basicSlots = findFreeTimes(validEvents, room.getStartTime(), room.getEndTime());

        return enrichSlotsWithMembers(basicSlots, room);
    }

    /**
     * 특정 방에서 완전히 비어있는 날짜 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<LocalDate> findFreeDates(String roomUuid) {
        Room room = findRoomByUuid(roomUuid);
        List<Event> events = getValidEvents(room);

        return findFreeDatesInRange(room, events);
    }

    /**
     * 특정 일정의 무시 상태를 토글합니다.
     * 무시 상태면 해제하고, 해제 상태면 무시 처리합니다.
     */
    @Transactional
    public void toggleIgnoreEvent(String roomUuid, Long memberId, String externalEventId) {
        Room room = findRoomByUuid(roomUuid);
        Member member = findMemberById(memberId);

        ignoredEventRepository.findByRoomAndMemberAndExternalEventId(room, member, externalEventId)
                .ifPresentOrElse(
                        ignoredEventRepository::delete,
                        () -> ignoredEventRepository.save(new IgnoredEvent(room, member, externalEventId)));
    }

    /**
     * 특정 멤버의 모든 일정과 무시 상태를 함께 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<EventResponseDto> getMemberEventsWithState(String uuid, Long memberId) {
        Room room = findRoomByUuid(uuid);
        Member member = findMemberById(memberId);

        List<Event> allEvents = eventRepository.findAllByMember(member);
        Set<String> ignoredIds = getIgnoredEventIds(room, member);

        return allEvents.stream()
                .map(event -> EventResponseDto.from(event, ignoredIds.contains(event.getExternalId())))
                .toList();
    }

    // ==================== 조회 헬퍼 메서드 ====================

    private Room findRoomByUuid(String uuid) {
        return roomRepository.findByRoomUUID(uuid)
                .orElseThrow(() -> new EntityNotFoundException("방을 찾을 수 없습니다: " + uuid));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("멤버를 찾을 수 없습니다: " + memberId));
    }

    // ==================== 이벤트 필터링 ====================

    /**
     * 방의 모든 멤버들의 유효한(무시되지 않은) 일정을 조회합니다.
     */
    private List<Event> getValidEvents(Room room) {
        List<RoomMember> roomMembers = roomMemberRepository.findAllByRoom(room);

        return roomMembers.stream()
                .flatMap(rm -> getValidEventsForMember(room, rm.getMember()).stream())
                .toList();
    }

    /**
     * 특정 멤버의 유효한 일정만 필터링합니다.
     */
    private List<Event> getValidEventsForMember(Room room, Member member) {
        List<Event> allEvents = eventRepository.findAllByMember(member);
        Set<String> ignoredIds = getIgnoredEventIds(room, member);

        return allEvents.stream()
                .filter(event -> !ignoredIds.contains(event.getExternalId()))
                .toList();
    }

    /**
     * 특정 방/멤버의 무시된 이벤트 ID 목록을 조회합니다.
     */
    private Set<String> getIgnoredEventIds(Room room, Member member) {
        return ignoredEventRepository.findByRoomAndMember(room, member).stream()
                .map(IgnoredEvent::getExternalEventId)
                .collect(Collectors.toSet());
    }

    // ==================== 빈 시간 계산 ====================

    /**
     * 일정 목록에서 빈 시간대를 찾습니다.
     * 스위핑 알고리즘을 사용하여 효율적으로 계산합니다.
     */
    private List<TimeSlotDto> findFreeTimes(List<Event> events, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        List<TimeSlotDto> freeTimes = new ArrayList<>();
        List<TimePoint> points = convertEventsToTimePoints(events);
        points.sort(TimePoint.comparator());

        int activeEventCount = 0;
        LocalDateTime freeStartTime = rangeStart;

        for (TimePoint point : points) {
            if (isFreeTimeEnding(activeEventCount, point)) {
                addFreeSlotIfValid(freeTimes, freeStartTime, point.time(), rangeStart, rangeEnd);
            }

            activeEventCount += point.type();

            if (isFreeTimeStarting(activeEventCount)) {
                freeStartTime = point.time();
            }
        }

        // 마지막 남은 빈 시간 처리
        addFreeSlotIfValid(freeTimes, freeStartTime, rangeEnd, rangeStart, rangeEnd);

        log.debug("빈 시간대 {} 개 발견", freeTimes.size());
        return freeTimes;
    }

    /**
     * 이벤트 목록을 TimePoint 목록으로 변환합니다.
     */
    private List<TimePoint> convertEventsToTimePoints(List<Event> events) {
        List<TimePoint> points = new ArrayList<>();
        for (Event event : events) {
            points.add(new TimePoint(event.getStartTime(), TimePoint.START));
            points.add(new TimePoint(event.getEndTime(), TimePoint.END));
        }
        return points;
    }

    private boolean isFreeTimeEnding(int activeCount, TimePoint point) {
        return activeCount == 0 && point.type() == TimePoint.START;
    }

    private boolean isFreeTimeStarting(int activeCount) {
        return activeCount == 0;
    }

    /**
     * 유효한 빈 시간대를 목록에 추가합니다.
     */
    private void addFreeSlotIfValid(List<TimeSlotDto> slots, LocalDateTime start, LocalDateTime end,
                                    LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        LocalDateTime validStart = start.isBefore(rangeStart) ? rangeStart : start;
        LocalDateTime validEnd = end.isAfter(rangeEnd) ? rangeEnd : end;

        if (validStart.isBefore(validEnd)) {
            slots.add(new TimeSlotDto(validStart, validEnd));
        }
    }

    // ==================== 빈 날짜 계산 ====================

    /**
     * 방의 범위 내에서 완전히 비어있는 날짜를 찾습니다.
     */
    private List<LocalDate> findFreeDatesInRange(Room room, List<Event> events) {
        List<LocalDate> freeDates = new ArrayList<>();

        LocalDate current = room.getStartTime().toLocalDate();
        LocalDate end = room.getEndTime().toLocalDate();

        while (!current.isAfter(end)) {
            if (!hasEventConflictOnDate(events, current)) {
                freeDates.add(current);
            }
            current = current.plusDays(1);
        }

        return freeDates;
    }

    /**
     * 특정 날짜에 일정 충돌이 있는지 확인합니다.
     */
    private boolean hasEventConflictOnDate(List<Event> events, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        return events.stream()
                .anyMatch(event -> isTimeRangeOverlapping(
                        event.getStartTime(), event.getEndTime(),
                        dayStart, dayEnd));
    }

    /**
     * 두 시간 범위가 겹치는지 확인합니다.
     */
    private boolean isTimeRangeOverlapping(LocalDateTime start1, LocalDateTime end1,
                                           LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    // ==================== 멤버 가용성 계산 ====================

    /**
     * 각 빈 시간대에 참여 가능한 멤버 정보를 추가합니다.
     */
    private List<TimeSlotDto> enrichSlotsWithMembers(List<TimeSlotDto> slots, Room room) {
        return slots.stream()
                .map(slot -> new TimeSlotDto(
                        slot.start(),
                        slot.end(),
                        findAvailableMembers(slot.start(), slot.end(), room),
                        0))
                .toList();
    }

    /**
     * 특정 시간대에 참여 가능한 멤버 목록을 조회합니다.
     */
    private List<MemberDto> findAvailableMembers(LocalDateTime slotStart, LocalDateTime slotEnd, Room room) {
        List<MemberDto> availableMembers = new ArrayList<>();

        for (RoomMember rm : roomMemberRepository.findAllByRoom(room)) {
            Member member = rm.getMember();

            if (!hasMemberConflict(room, member, slotStart, slotEnd)) {
                availableMembers.add(toMemberDto(member));
            }
        }

        return availableMembers;
    }

    /**
     * 멤버가 해당 시간대에 다른 일정이 있는지 확인합니다.
     */
    private boolean hasMemberConflict(Room room, Member member, LocalDateTime slotStart, LocalDateTime slotEnd) {
        List<Event> events = getValidEventsForMember(room, member);

        return events.stream()
                .anyMatch(event -> isTimeRangeOverlapping(
                        event.getStartTime(), event.getEndTime(),
                        slotStart, slotEnd));
    }

    /**
     * Member 엔티티를 MemberDto로 변환합니다.
     */
    private MemberDto toMemberDto(Member member) {
        MemberType type = (member.getServiceType() != null)
                ? MemberType.valueOf(member.getServiceType().name())
                : MemberType.GUEST;

        return new MemberDto(member.getId(), member.getName(), type);
    }
}
