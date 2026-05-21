-- 로컬 Postgres에서 한 번 실행 (pgAdmin 또는 psql)
-- 앱 실행 전: CREATE EXTENSION IF NOT EXISTS postgis;

-- ERD: latitude / longitude 컬럼 기준 공간 인덱스 (표현식 GIST)
CREATE INDEX IF NOT EXISTS idx_restaurants_latlng_gist
    ON restaurants USING GIST (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326));
