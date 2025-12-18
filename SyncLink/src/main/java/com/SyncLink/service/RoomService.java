package com.SyncLink.service;

import com.SyncLink.domain.Member;
import com.SyncLink.domain.Room;
import com.SyncLink.infrastructure.EventRepository;
import com.SyncLink.infrastructure.MemberRepository;
import com.SyncLink.infrastructure.RoomRepository;
import com.SyncLink.presentation.RoomCreateRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final EventService eventService;

    @Transactional
    public String createRoom(RoomCreateRequest request, String email){

        // 방 할당 고유주소
        String roomUuid = UUID.randomUUID().toString();

        Room room = Room.builder()
                .roomUUID(roomUuid)
                .title(request.title())
                .startTime(request.startDateTime())
                .endTime(request.endDateTime())
                .build();

        roomRepository.save(room);

        // 저장한 뒤에 방을 만든 방장에게도 방 번호 배정
        Member bangjang = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않는다."));

        bangjang.setRoom(room);

        return roomUuid;
    }



}
