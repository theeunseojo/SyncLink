package com.SyncLink.infrastructure;

import com.SyncLink.domain.Event;
import com.SyncLink.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
     void deleteByMember(Member member);
     List<Event> findAllByMember(Member member);
}
