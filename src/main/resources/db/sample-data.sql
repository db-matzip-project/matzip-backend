-- Execute manually for demo/testing.
-- This file is intentionally not auto-wired in application.properties.

INSERT INTO users (nickname, age) VALUES
('alice', 24),
('bora', 26),
('chul', 27),
('dami', 22),
('eric', 25),
('fiona', 31);

INSERT INTO preferences (code, display_name) VALUES
('SPICY_HIGH', '매운맛 매우 선호'),
('SPICY_MEDIUM', '매운맛 보통 선호'),
('SWEET_HIGH', '단맛 선호'),
('SALTY_HIGH', '짠맛 선호');

INSERT INTO user_preferences (user_id, preference_id, weight)
SELECT u.id, p.id, 5
FROM users u
JOIN preferences p ON p.code = 'SPICY_HIGH'
WHERE u.nickname IN ('alice', 'bora', 'chul', 'eric');

INSERT INTO user_preferences (user_id, preference_id, weight)
SELECT u.id, p.id, 3
FROM users u
JOIN preferences p ON p.code = 'SPICY_MEDIUM'
WHERE u.nickname IN ('dami', 'fiona');

INSERT INTO restaurants (name, address) VALUES
('청양칼국수', '서울 어딘가 1'),
('홍염떡볶이', '서울 어딘가 2'),
('마라광장', '서울 어딘가 3'),
('핫라멘하우스', '서울 어딘가 4'),
('불닭연구소', '서울 어딘가 5'),
('순한국밥', '서울 어딘가 6');

-- Recent schedules (within one month).
INSERT INTO schedules (user_id, restaurant_id, visit_date_time)
SELECT u.id, r.id, NOW() - INTERVAL '3 day'
FROM users u
JOIN restaurants r ON r.name = '청양칼국수'
WHERE u.nickname IN ('bora', 'chul', 'eric');

INSERT INTO schedules (user_id, restaurant_id, visit_date_time)
SELECT u.id, r.id, NOW() - INTERVAL '5 day'
FROM users u
JOIN restaurants r ON r.name = '홍염떡볶이'
WHERE u.nickname IN ('bora', 'chul');

INSERT INTO schedules (user_id, restaurant_id, visit_date_time)
SELECT u.id, r.id, NOW() - INTERVAL '7 day'
FROM users u
JOIN restaurants r ON r.name = '마라광장'
WHERE u.nickname IN ('bora', 'eric');

INSERT INTO schedules (user_id, restaurant_id, visit_date_time)
SELECT u.id, r.id, NOW() - INTERVAL '8 day'
FROM users u
JOIN restaurants r ON r.name = '핫라멘하우스'
WHERE u.nickname IN ('chul', 'eric');

-- Old schedules (excluded by 1 month filter).
INSERT INTO schedules (user_id, restaurant_id, visit_date_time)
SELECT u.id, r.id, NOW() - INTERVAL '45 day'
FROM users u
JOIN restaurants r ON r.name = '불닭연구소'
WHERE u.nickname IN ('bora', 'chul', 'eric');
