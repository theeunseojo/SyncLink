-- ============================================
-- SyncLink 인덱스 생성 스크립트
-- 쿼리 튜닝을 위한 인덱스
-- ============================================

-- Event 테이블 인덱스
CREATE INDEX idx_event_member_id ON event(member_id);
CREATE INDEX idx_event_external_id ON event(external_id);
CREATE INDEX idx_event_start_end ON event(start_time, end_time);

-- Room 테이블 인덱스
CREATE INDEX idx_room_uuid ON room(room_uuid);

-- RoomMember 테이블 인덱스
CREATE INDEX idx_room_member_room ON room_member(room_id);
CREATE INDEX idx_room_member_member ON room_member(member_id);
CREATE INDEX idx_room_member_room_member ON room_member(room_id, member_id);

-- IgnoredEvent 테이블 인덱스
CREATE INDEX idx_ignored_event_room_member ON ignored_event(room_id, member_id);

-- Member 테이블 인덱스
CREATE INDEX idx_member_email ON member(email);
