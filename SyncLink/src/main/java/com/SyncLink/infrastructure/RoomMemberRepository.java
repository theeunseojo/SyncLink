package com.SyncLink.infrastructure;

import com.SyncLink.domain.Member;
import com.SyncLink.domain.Room;
import com.SyncLink.domain.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {

    // 특정 방의 모든 참여자 조회
    List<RoomMember> findAllByRoom(Room room);

    // 특정 멤버가 특정 방에 참여했는지 확인
    Optional<RoomMember> findByRoomAndMember(Room room, Member member);

    // 특정 멤버가 참여한 모든 방 조회
    List<RoomMember> findAllByMember(Member member);

    // 특정 방에 멤버가 존재하는지 확인
    boolean existsByRoomAndMember(Room room, Member member);
}
