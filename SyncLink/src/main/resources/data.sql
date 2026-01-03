-- ============================================
-- SyncLink 더미 데이터 생성 스크립트 (MySQL CLI용)
-- Member 1,000명 / Room 100개 / RoomMember 5,000개 / Event 100,000개
-- ============================================

USE synclinkdb;

-- Member 1,000명 생성
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS generate_members()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 1000 DO
        INSERT INTO member (email, name, service_type, token)
        VALUES (
            CONCAT('user', i, '@test.com'),
            CONCAT('테스터', i),
            'GOOGLE',
            CONCAT('token_', i)
        );
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

CALL generate_members();
DROP PROCEDURE IF EXISTS generate_members;

-- Room 100개 생성
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS generate_rooms()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE start_date DATETIME;
    WHILE i <= 100 DO
        SET start_date = DATE_ADD(NOW(), INTERVAL FLOOR(RAND() * 30) DAY);
        INSERT INTO room (roomuuid, title, start_time, end_time, mode, host_id)
        VALUES (
            UUID(),
            CONCAT('회의실 ', i),
            start_date,
            DATE_ADD(start_date, INTERVAL 7 DAY),
            0,
            FLOOR(RAND() * 1000) + 1
        );
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

CALL generate_rooms();
DROP PROCEDURE IF EXISTS generate_rooms;

-- RoomMember 5,000개 생성
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS generate_room_members()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE room_count INT DEFAULT 1;
    DECLARE member_offset INT;
    WHILE room_count <= 100 DO
        SET member_offset = (room_count - 1) * 10;
        SET i = 1;
        WHILE i <= 50 DO
            INSERT IGNORE INTO room_member (room_id, member_id, joined_at, is_host)
            VALUES (
                room_count,
                ((member_offset + i - 1) MOD 1000) + 1,
                NOW(),
                IF(i = 1, 1, 0)
            );
            SET i = i + 1;
        END WHILE;
        SET room_count = room_count + 1;
    END WHILE;
END //
DELIMITER ;

CALL generate_room_members();
DROP PROCEDURE IF EXISTS generate_room_members;

-- Event 100,000개 생성
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS generate_events()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE member_id INT;
    DECLARE event_start DATETIME;
    WHILE i <= 100000 DO
        SET member_id = FLOOR(RAND() * 1000) + 1;
        SET event_start = DATE_ADD(NOW(), INTERVAL FLOOR(RAND() * 60 - 30) DAY);
        INSERT INTO event (member_id, title, start_time, end_time, external_id)
        VALUES (
            member_id,
            CONCAT('일정 ', i),
            event_start,
            DATE_ADD(event_start, INTERVAL FLOOR(RAND() * 4 + 1) HOUR),
            CONCAT('ext_', i)
        );
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

CALL generate_events();
DROP PROCEDURE IF EXISTS generate_events;

-- IgnoredEvent 1,000개 생성
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS generate_ignored_events()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 1000 DO
        INSERT IGNORE INTO ignored_event (room_id, member_id, external_event_id)
        VALUES (
            FLOOR(RAND() * 100) + 1,
            FLOOR(RAND() * 1000) + 1,
            CONCAT('ext_', FLOOR(RAND() * 100000) + 1)
        );
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

CALL generate_ignored_events();
DROP PROCEDURE IF EXISTS generate_ignored_events;

-- 생성 결과 확인
SELECT 'member' AS table_name, COUNT(*) AS count FROM member
UNION ALL
SELECT 'room', COUNT(*) FROM room
UNION ALL
SELECT 'room_member', COUNT(*) FROM room_member
UNION ALL
SELECT 'event', COUNT(*) FROM event
UNION ALL
SELECT 'ignored_event', COUNT(*) FROM ignored_event;
