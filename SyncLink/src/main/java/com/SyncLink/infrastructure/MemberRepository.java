package com.SyncLink.infrastructure;

import com.SyncLink.domain.Member;
import com.SyncLink.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository <Member,Long>{
    Optional<Member> findByEmail(String email);
    List<Member> findAllByRoom(Room room);
}
