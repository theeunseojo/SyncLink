package com.SyncLink.infrastructure;

import com.SyncLink.domain.IgnoredEvent;
import com.SyncLink.domain.Member;
import com.SyncLink.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IgnoredEventRepository extends JpaRepository<IgnoredEvent, Long>{
    List<IgnoredEvent> findByRoomAndMember(Room room, Member member);
    Optional<IgnoredEvent> findByRoomAndMemberAndGoogleEventId(Room room, Member member, String googleEventId);
}
