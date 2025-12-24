package com.SyncLink.service;

import com.SyncLink.domain.Member;
import com.SyncLink.domain.Room;
import com.SyncLink.domain.RoomMember;
import com.SyncLink.infrastructure.MemberRepository;
import com.SyncLink.infrastructure.RoomMemberRepository;
import com.SyncLink.infrastructure.RoomRepository;
import com.SyncLink.presentation.RoomCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {
        private final RoomRepository roomRepository;
        private final MemberRepository memberRepository;
        private final RoomMemberRepository roomMemberRepository;

        @Transactional
        public String createRoom(RoomCreateRequest request, String email) {
                // 방 할당 고유주소
                String roomUuid = UUID.randomUUID().toString();

                Room room = Room.builder()
                                .roomUUID(roomUuid)
                                .title(request.title())
                                .startTime(request.startDateTime())
                                .endTime(request.endDateTime())
                                .build();

                roomRepository.save(room);

                // 방장을 RoomMember로 등록
                Member bangjang = memberRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

                RoomMember roomMember = RoomMember.builder()
                                .room(room)
                                .member(bangjang)
                                .isHost(true) // 방장 표시
                                .build();

                roomMemberRepository.save(roomMember);

                return roomUuid;
        }

        @Transactional(readOnly = true)
        public List<String> getMemberByRoom(String uuid) {
                Room room = roomRepository.findByRoomUUID(uuid)
                                .orElseThrow(() -> new IllegalArgumentException("방이 존재하지 않습니다."));

                // RoomMember를 통해 참여자 조회
                return roomMemberRepository.findAllByRoom(room)
                                .stream()
                                .map(rm -> rm.getMember().getName())
                                .toList();
        }

        // 방 참여 (새 멤버 추가)
        @Transactional
        public void joinRoom(String uuid, Member member) {
                Room room = roomRepository.findByRoomUUID(uuid)
                                .orElseThrow(() -> new IllegalArgumentException("방이 존재하지 않습니다."));

                // 이미 참여 중인지 확인
                if (roomMemberRepository.existsByRoomAndMember(room, member)) {
                        return; // 이미 참여 중이면 무시
                }

                RoomMember roomMember = RoomMember.builder()
                                .room(room)
                                .member(member)
                                .isHost(false)
                                .build();

                roomMemberRepository.save(roomMember);
        }
}
