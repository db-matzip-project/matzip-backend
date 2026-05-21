-- schedule_restaurants 에 INSERT 될 때마다 해당 식당의 schedule_add_count +1
-- 앱으로 테이블 생성 후(pgAdmin 등에서) 실행하세요.

CREATE OR REPLACE FUNCTION trg_increment_restaurant_schedule_add_count()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE restaurants
    SET schedule_add_count = COALESCE(schedule_add_count, 0) + 1
    WHERE id = NEW.restaurant_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_schedule_restaurants_after_insert ON schedule_restaurants;

CREATE TRIGGER trg_schedule_restaurants_after_insert
    AFTER INSERT ON schedule_restaurants
    FOR EACH ROW
    EXECUTE FUNCTION trg_increment_restaurant_schedule_add_count();
