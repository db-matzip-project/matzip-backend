-- restaurants.category 백필 정규화 스크립트
-- 목적: 기존 카테고리 데이터(FD6, 영문, 원본 문자열)를
--      프론트 필터 기준 카테고리(한식/일식/중식/양식/채식/디저트/기타)로 일괄 정리
--
-- 실행 예시:
-- psql -h localhost -p 5432 -U postgres -d matzip_db -f "src/main/resources/db/normalize-restaurant-categories.sql"

BEGIN;

UPDATE restaurants
SET category =
    CASE
        -- 디저트/카페
        WHEN LOWER(COALESCE(category, '') || ' ' || COALESCE(description, '')) SIMILAR TO
             '%(디저트|카페|베이커리|빵|케이크|커피|tea|dessert|cafe|bakery)%'
            THEN '디저트'

        -- 채식
        WHEN LOWER(COALESCE(category, '') || ' ' || COALESCE(description, '')) SIMILAR TO
             '%(채식|비건|샐러드|vegetarian|vegan)%'
            THEN '채식'

        -- 일식
        WHEN LOWER(COALESCE(category, '') || ' ' || COALESCE(description, '')) SIMILAR TO
             '%(일식|일본|초밥|스시|라멘|돈까스|이자카야|japanese)%'
            THEN '일식'

        -- 중식
        WHEN LOWER(COALESCE(category, '') || ' ' || COALESCE(description, '')) SIMILAR TO
             '%(중식|중국|짜장|짬뽕|마라|딤섬|chinese)%'
            THEN '중식'

        -- 양식
        WHEN LOWER(COALESCE(category, '') || ' ' || COALESCE(description, '')) SIMILAR TO
             '%(양식|이탈|프랑|스테이크|파스타|피자|브런치|western|italian|french)%'
            THEN '양식'

        -- 한식
        WHEN LOWER(COALESCE(category, '') || ' ' || COALESCE(description, '')) SIMILAR TO
             '%(한식|국밥|찌개|분식|족발|보쌈|korean)%'
            THEN '한식'

        -- 그 외
        ELSE '기타'
    END;

COMMIT;
