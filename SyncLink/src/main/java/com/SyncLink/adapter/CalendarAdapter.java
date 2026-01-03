package com.SyncLink.adapter;

import com.SyncLink.domain.Event;
import com.SyncLink.domain.Member;
import com.SyncLink.enums.ServiceType;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarAdapter {
    List<Event> fetchEvents(Member member, LocalDateTime start, LocalDateTime end) throws Exception;
    boolean supports(ServiceType type);
}
