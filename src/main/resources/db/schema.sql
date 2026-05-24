-- =============================================================================
-- Matzip 정규 스키마 (PostgreSQL 13+ 권장, PostGIS 확장 별도)
-- 애플리케이션 JPA 엔티티(users, preferences, restaurants, schedules,
-- schedule_restaurants)와 1:1로 맞춘 DDL입니다.
--
-- 실행 순서 (수동 DDL 워크플로우):
--   1) 본 파일 schema.sql
--   2) postgis.sql (지도 bounds 검색용 GIST)
--   3) triggers.sql (schedule_add_count 동기화)
--
-- 로컬 개발에서 Hibernate ddl-auto=update 만 쓸 경우 이 파일 생략 가능하나,
-- DB 과제 제출·무결성 설명용으로 이 스크립트가 단일 기준(SSOT)입니다.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- users : 회원 + 감사 컬럼(생성·수정 시각)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    login_id        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(120) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    phone           VARCHAR(30)  NOT NULL,
    nickname        VARCHAR(50),
    age             INTEGER,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT LOCALTIMESTAMP,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT LOCALTIMESTAMP,
    CONSTRAINT chk_users_age CHECK (age IS NULL OR (age BETWEEN 1 AND 120))
);

COMMENT ON TABLE users IS '서비스 회원. OLAP 연령대 매칭은 age 가 NULL 이 아닐 때만 사용';
COMMENT ON COLUMN users.password_hash IS 'BCrypt 등 단방향 해시만 저장';

CREATE INDEX IF NOT EXISTS idx_users_age ON users (age) WHERE age IS NOT NULL;

-- ---------------------------------------------------------------------------
-- preferences / user_preferences : 취향 태그 정규화 (N:M)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS preferences (
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(50)  NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL
);

COMMENT ON TABLE preferences IS '취향 마스터(매운맛·단맛 등 코드)';
COMMENT ON TABLE user_preferences IS '회원–취향 매핑. 복합 PK 로 중복 방지';

CREATE TABLE IF NOT EXISTS user_preferences (
    user_id        BIGINT NOT NULL,
    preference_id  BIGINT NOT NULL,
    PRIMARY KEY (user_id, preference_id),
    CONSTRAINT fk_up_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_up_pref
        FOREIGN KEY (preference_id) REFERENCES preferences(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_preferences_pref ON user_preferences (preference_id);

-- ---------------------------------------------------------------------------
-- restaurants : 장소 마스터 + 집계 컬럼 schedule_add_count
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS restaurants (
    id                   BIGSERIAL PRIMARY KEY,
    api_id               VARCHAR(64) UNIQUE,
    name                 VARCHAR(200) NOT NULL,
    category             VARCHAR(80),
    address              VARCHAR(500),
    road_address         VARCHAR(500),
    phone                VARCHAR(40),
    description          VARCHAR(2000),
    latitude             DOUBLE PRECISION,
    longitude            DOUBLE PRECISION,
    rating               DOUBLE PRECISION,
    review_count         INTEGER,
    schedule_add_count   INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_restaurants_rating CHECK (rating IS NULL OR (rating >= 0 AND rating <= 5)),
    CONSTRAINT chk_restaurants_lat CHECK (latitude IS NULL OR (latitude BETWEEN -90 AND 90)),
    CONSTRAINT chk_restaurants_lon CHECK (longitude IS NULL OR (longitude BETWEEN -180 AND 180))
);

COMMENT ON COLUMN restaurants.schedule_add_count IS '일정에 담긴 횟수 denormalization; triggers.sql 로 동기화 권장';
COMMENT ON COLUMN restaurants.api_id IS '카카오 등 외부 장소 고유 ID (중복 적재 방지)';

CREATE INDEX IF NOT EXISTS idx_restaurants_category ON restaurants (category)
    WHERE category IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_restaurants_rating ON restaurants (rating DESC NULLS LAST);

-- ---------------------------------------------------------------------------
-- reviews : 사용자-식당 리뷰 (사용자당 식당 1개 리뷰)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reviews (
    id             BIGSERIAL PRIMARY KEY,
    restaurant_id  BIGINT NOT NULL,
    user_id        BIGINT NOT NULL,
    content        VARCHAR(1000) NOT NULL,
    rating         INTEGER NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT LOCALTIMESTAMP,
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT LOCALTIMESTAMP,
    CONSTRAINT fk_reviews_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_reviews_restaurant_user UNIQUE (restaurant_id, user_id),
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5)
);

COMMENT ON TABLE reviews IS '사용자-식당 리뷰. 사용자당 식당 1개 리뷰(수정 가능)';
COMMENT ON COLUMN reviews.rating IS '리뷰 평점(1~5)';

CREATE INDEX IF NOT EXISTS idx_reviews_restaurant_created_at
    ON reviews (restaurant_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_reviews_user_created_at
    ON reviews (user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_reviews_restaurant_rating
    ON reviews (restaurant_id, rating);

-- ---------------------------------------------------------------------------
-- schedules : 회원별 여행 일정 헤더
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS schedules (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    title       VARCHAR(200) NOT NULL,
    travel_date DATE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_schedules_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

COMMENT ON TABLE schedules IS '일정 헤더. 식당 나열은 schedule_restaurants 로 분리 (1NF·확장성)';

CREATE INDEX IF NOT EXISTS idx_schedules_user_created ON schedules (user_id, created_at DESC);

-- ---------------------------------------------------------------------------
-- schedule_restaurants : 일정–식당 매핑 + 방문 순서 + 분석용 added_at
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS schedule_restaurants (
    id             BIGSERIAL PRIMARY KEY,
    schedule_id    BIGINT NOT NULL,
    restaurant_id  BIGINT NOT NULL,
    visit_order    INTEGER NOT NULL,
    memo           VARCHAR(500),
    added_at       TIMESTAMPTZ,
    CONSTRAINT uq_sr_schedule_restaurant UNIQUE (schedule_id, restaurant_id),
    CONSTRAINT fk_sr_schedule
        FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    CONSTRAINT fk_sr_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE RESTRICT,
    CONSTRAINT chk_sr_visit_order CHECK (visit_order >= 1)
);

COMMENT ON COLUMN schedule_restaurants.added_at IS '분석 시점 기준; NULL 이면 schedules.created_at 과 COALESCE 로 보정';

CREATE INDEX IF NOT EXISTS idx_sr_schedule_visit ON schedule_restaurants (schedule_id, visit_order);
CREATE INDEX IF NOT EXISTS idx_sr_restaurant ON schedule_restaurants (restaurant_id);
CREATE INDEX IF NOT EXISTS idx_sr_added_at ON schedule_restaurants (added_at);
