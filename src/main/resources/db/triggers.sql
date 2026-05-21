-- =============================================================================
-- schedule_restaurants 변경 시 restaurants.schedule_add_count 동기화
-- 선행 조건: schema.sql 로 테이블 존재
--
-- PostgreSQL 14+: EXECUTE FUNCTION
-- PostgreSQL 11~13: 아래 트리거 정의에서 EXECUTE FUNCTION 을 EXECUTE PROCEDURE 로 교체
-- =============================================================================

CREATE OR REPLACE FUNCTION trg_schedule_restaurants_adjust_schedule_add_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE restaurants
        SET schedule_add_count = COALESCE(schedule_add_count, 0) + 1
        WHERE id = NEW.restaurant_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE restaurants
        SET schedule_add_count = GREATEST(COALESCE(schedule_add_count, 0) - 1, 0)
        WHERE id = OLD.restaurant_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_schedule_restaurants_ai_ad ON schedule_restaurants;

CREATE TRIGGER trg_schedule_restaurants_ai_ad
    AFTER INSERT OR DELETE ON schedule_restaurants
    FOR EACH ROW
    EXECUTE FUNCTION trg_schedule_restaurants_adjust_schedule_add_count();

COMMENT ON FUNCTION trg_schedule_restaurants_adjust_schedule_add_count() IS
    '일정에 식당이 추가되면 schedule_add_count 증가, 매핑 행 삭제 시 감소(비음수 클램프)';
