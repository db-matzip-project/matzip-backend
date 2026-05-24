-- =============================================================================
-- 데모 데이터 (수동 실행)
-- 선행: schema.sql → postgis.sql → triggers.sql 순 적용 후 실행 권장.
--       또는 Hibernate ddl-auto=update 로 테이블 생성 후 실행.
--
-- 비밀번호 해시는 Laravel 테스트 벡터로 평문 **password** 와 대응되는 경우가 많습니다.
-- 로그인 안 되면 JWT 회원가입 API 로 계정을 새로 만드는 것을 권장합니다.
-- =============================================================================

BEGIN;

INSERT INTO preferences (code, display_name) VALUES
    ('SPICY_LOW', '약간 매운 편'),
    ('SPICY_MED', '보통 매움'),
    ('SPICY_HIGH', '아주 매운 편'),
    ('SWEET', '단 맛 선호'),
    ('SALTY', '짠 맛 선호'),
    ('LIGHT', '담백한 맛'),
    ('OILY', '기름진 음식 OK'),
    ('RAW_OK', '회·사시미 좋아함'),
    ('NO_PORK', '돼지고기 비선호'),
    ('VEGETARIAN', '채식 위주')
ON CONFLICT (code) DO NOTHING;

INSERT INTO users (login_id, password_hash, name, phone, nickname, age)
VALUES
    ('alice', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '김앨리스', '01011111111', 'alice', 24),
    ('bora', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '이보라', '01022222222', 'bora', 26),
    ('chul', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '박철', '01033333333', 'chul', 27),
    ('eric', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '윤에릭', '01044444444', 'eric', 25)
ON CONFLICT (login_id) DO NOTHING;

INSERT INTO user_preferences (user_id, preference_id)
SELECT u.id, p.id
FROM users u
JOIN preferences p ON p.code = 'SPICY_HIGH'
WHERE u.login_id IN ('alice', 'bora', 'chul', 'eric')
ON CONFLICT DO NOTHING;

INSERT INTO user_preferences (user_id, preference_id)
SELECT u.id, p.id
FROM users u
JOIN preferences p ON p.code = 'SPICY_MED'
WHERE u.login_id IN ('alice', 'bora')
ON CONFLICT DO NOTHING;

INSERT INTO restaurants (api_id, name, category, address, road_address, phone, latitude, longitude, rating, review_count, schedule_add_count)
VALUES
    ('demo-1', '청양칼국수', 'KOREAN', '서울시 어디', '테헤란로', '02-1111-1111', 37.5665, 126.9780, 4.5, 120, 0),
    ('demo-2', '홍염떡볶이', 'SNACK', '서울시 어디', '강남대로', '02-2222-2222', 37.4980, 127.0276, 4.3, 88, 0),
    ('demo-3', '마라광장', 'CHINESE', '서울시 어디', '홍대입구', '02-3333-3333', 37.5571, 126.9244, 4.1, 64, 0),
    ('demo-4', '핫라멘하우스', 'JAPANESE', '서울시 어디', '신촌로', '02-4444-4444', 37.5597, 126.9423, 4.6, 210, 0),
    ('demo-5', '순한국밥', 'KOREAN', '서울시 어디', '종로', '02-5555-5555', 37.5735, 126.9790, 4.0, 42, 0)
ON CONFLICT (api_id) DO NOTHING;

-- bora 일정 1건 + 식당 2곳 (added_at 최근 → OLAP 윈도우 통과)
INSERT INTO schedules (user_id, title, travel_date, created_at)
SELECT id, '맛집 투어 A', CURRENT_DATE, NOW() - INTERVAL '10 day'
FROM users WHERE login_id = 'bora';

INSERT INTO schedule_restaurants (schedule_id, restaurant_id, visit_order, memo, added_at)
SELECT sch.id, r.id, 1, '점심', NOW() - INTERVAL '3 day'
FROM schedules sch
JOIN users u ON sch.user_id = u.id AND u.login_id = 'bora'
JOIN restaurants r ON r.api_id = 'demo-1'
WHERE sch.id = (SELECT MAX(s2.id) FROM schedules s2 WHERE s2.user_id = u.id);

INSERT INTO schedule_restaurants (schedule_id, restaurant_id, visit_order, added_at)
SELECT sch.id, r.id, 2, NOW() - INTERVAL '2 day'
FROM schedules sch
JOIN users u ON sch.user_id = u.id AND u.login_id = 'bora'
JOIN restaurants r ON r.api_id = 'demo-3'
WHERE sch.id = (SELECT MAX(s2.id) FROM schedules s2 WHERE s2.user_id = u.id);

-- eric 일정 + 데모-2 (유사 사용자 집계용 추가 행들)
INSERT INTO schedules (user_id, title, travel_date, created_at)
SELECT id, '주말 코스', CURRENT_DATE + 7, NOW() - INTERVAL '8 day'
FROM users WHERE login_id = 'eric';

INSERT INTO schedule_restaurants (schedule_id, restaurant_id, visit_order, added_at)
SELECT sch.id, r.id, 1, NOW() - INTERVAL '4 day'
FROM schedules sch
JOIN users u ON sch.user_id = u.id AND u.login_id = 'eric'
JOIN restaurants r ON r.api_id = 'demo-2'
WHERE sch.id = (SELECT MAX(s2.id) FROM schedules s2 WHERE s2.user_id = u.id);

INSERT INTO schedules (user_id, title, travel_date, created_at)
SELECT id, '회식', CURRENT_DATE + 14, NOW() - INTERVAL '6 day'
FROM users WHERE login_id = 'chul';

INSERT INTO schedule_restaurants (schedule_id, restaurant_id, visit_order, added_at)
SELECT sch.id, r.id, 1, NOW() - INTERVAL '5 day'
FROM schedules sch
JOIN users u ON sch.user_id = u.id AND u.login_id = 'chul'
JOIN restaurants r ON r.api_id = 'demo-1'
WHERE sch.id = (SELECT MAX(s2.id) FROM schedules s2 WHERE s2.user_id = u.id);

COMMIT;

-- 트리거 없이 초기 적재만 했다면 집계 컬럼 재계산:
-- UPDATE restaurants r SET schedule_add_count = (
--     SELECT COUNT(*)::int FROM schedule_restaurants sr WHERE sr.restaurant_id = r.id
-- );
