-- =============================================================================
-- PostGIS 확장 + 식당 위치 표현식 GIST 인덱스
-- 선행 조건: schema.sql 로 restaurants 테이블 존재
-- 실행 예: psql -d matzip_db -f postgis.sql
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS postgis;

-- 네이티브 검색: ST_Contains(ST_MakeEnvelope(...), ST_SetSRID(ST_MakePoint(longitude,latitude),4326))
-- 실제 컬럼 pair 대신 표현식 인덱스 → 조건과 동일한 식을 인덱스 키로 사용해야 타킷팅됩니다.
CREATE INDEX IF NOT EXISTS idx_restaurants_latlng_gist
    ON restaurants USING GIST (
        ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
    );

COMMENT ON INDEX idx_restaurants_latlng_gist IS '지도 bbox 검색(ST_Contains envelope vs point)용 공간 인덱스';
