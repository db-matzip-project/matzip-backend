-- 예외 보정용: Hibernate 마이그레이션 중 schedule_add_count 컬럼이 NULL 상태로만 존재할 때 1회 실행.
-- 정상 적재 경로에서는 schema.sql 기준으로 생성되는 경우가 많아 필요 없을 수 있습니다.
-- Hibernate 가 이미 nullable 컬럼만 추가한 뒤 NOT NULL 로 바꾸지 못할 때 한 번만 실행.
UPDATE restaurants SET schedule_add_count = 0 WHERE schedule_add_count IS NULL;
ALTER TABLE restaurants ALTER COLUMN schedule_add_count SET DEFAULT 0;
ALTER TABLE restaurants ALTER COLUMN schedule_add_count SET NOT NULL;
