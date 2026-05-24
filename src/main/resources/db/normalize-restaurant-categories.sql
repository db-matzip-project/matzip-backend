-- restaurants.category 백필 정규화 스크립트
-- 목적: 기존 카테고리 데이터(FD6, 영문, 원본 문자열)를
--      한식·일식·중식·양식·채식·디저트·기타 중 하나로 정리합니다. 패턴 매칭이 안 되면 기타 로 둡니다.
--
-- 실행 예시:
-- psql -h localhost -p 5432 -U postgres -d matzip_db -f "src/main/resources/db/normalize-restaurant-categories.sql"

BEGIN;

UPDATE restaurants
SET category = '디저트'
WHERE trim(upper(category)) = 'CE7';

UPDATE restaurants
SET category =
    CASE
        -- 백화점·몰 등 (이름까지 본다 — category 에 잘못 '한식'만 박혀 있어도 잡히게)
        WHEN LOWER(
                        COALESCE(name, '')
                        || ' '
                        || COALESCE(category, '')
                        || ' '
                        || COALESCE(description, '')
                )
                SIMILAR TO '%(백화점|대형매장|쇼핑몰|쇼핑센터|아울렛|department store|shopping mall|shopping center)%'
            THEN '기타'

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
             '%(양식|이탈|프랑|스테이크|파스타|피자|브런치|패스트푸드|fast food|fastfood|burger|western|italian|french)%'
            THEN '양식'

        -- 한식
        WHEN LOWER(COALESCE(category, '') || ' ' || COALESCE(description, '')) SIMILAR TO
             '%(한식|국밥|찌개|분식|족발|보쌈|korean)%'
            THEN '한식'

        -- 그 외
        ELSE '기타'
    END;

COMMIT;
