# 데이터베이스 설계 (DB 과제 제출용)

이 프로젝트는 **회원–취향(N:M)**, **식당 마스터**, **일정 헤더–일정별 식당 매핑**으로 정규화되어 있습니다. 지도 검색은 **PostGIS 표현식 GIST 인덱스**, 일정 편의 기능은 **`schedule_add_count` 비정규화 + 트리거**로 설명할 수 있습니다.

## 1. 실행 순서 (PostgreSQL)

| 순서 | 파일 | 역할 |
|------|------|------|
| 1 | [`schema.sql`](../src/main/resources/db/schema.sql) | 테이블·PK/FK/CHECK·B-tree 보조 인덱스 |
| 2 | [`postgis.sql`](../src/main/resources/db/postgis.sql) | `CREATE EXTENSION postgis` + bbox 검색용 GIST |
| 3 | [`triggers.sql`](../src/main/resources/db/triggers.sql) | `schedule_restaurants` 삽입/삭제 시 `restaurants.schedule_add_count` 동기화 |

기존 DB에 레거시 `restaurants.location`(geometry 등) 컬럼만 남아 있다면 선택적으로 [`drop-restaurants-location-column.sql`](../src/main/resources/db/drop-restaurants-location-column.sql)을 실행합니다(앱과 SSOT DDL에는 해당 컬럼 없음).

기존 배포에 `users.nickname`, `users.age` 가 남아 있으면 선택적으로 [`drop-users-nickname-age-columns.sql`](../src/main/resources/db/drop-users-nickname-age-columns.sql) 을 실행합니다.

레거시 평면 스키마(비교용): [`schema_legacy_flat_schedules.sql`](../src/main/resources/db/schema_legacy_flat_schedules.sql)

데모 데이터: [`sample-data.sql`](../src/main/resources/db/sample-data.sql) — **빈 DB 또는 초기화 후 1회** 실행 권장.

이전 파일명 호환:

- [`dev-postgis.sql`](../src/main/resources/db/dev-postgis.sql) → `postgis.sql` 안내용
- [`schedule-restaurant-trigger.sql`](../src/main/resources/db/schedule-restaurant-trigger.sql) → `triggers.sql` 안내용

## 2. 정규화·무결성 요약

- **`preferences` / `user_preferences`**: 취향 태그를 열 문자열로 붙이지 않고 **마스터 + 매핑 테이블**로 분리 → 태그 변경·검색 시 중복·오타 감소.
- **`schedules` vs `schedule_restaurants`**: 한 일정에 여러 식당을 넣으려면 **일정 헤더와 매핑 행을 분리**해야 합니다(1NF·실무 확장성).
- **FK**
  - `schedules.user_id` → `users` (**ON DELETE CASCADE**: 회원 삭제 시 일정 정리)
  - `schedule_restaurants.schedule_id` → `schedules` (**CASCADE**)
  - `schedule_restaurants.restaurant_id` → `restaurants` (**RESTRICT**: 참조 중인 식당 삭제 방지)
- **비즈니스 규칙(CHECK)**  
  `restaurants.rating/lat/lon`, `schedule_restaurants.visit_order` 등 단순 도메인 오류를 DB 단에서 한 번 더 차단합니다.

## 3. 인덱스 전략

| 인덱스 | 목적 |
|--------|------|
| `idx_schedules_user_created` | 사용자별 일정 목록 정렬 (`user_id`, `created_at DESC`) |
| `idx_sr_schedule_visit` | 한 일정 안에서 `visit_order` 순 조회 |
| `idx_sr_restaurant` | 특정 식당이 들어간 일정 매핑 역방향 탐색 |
| `idx_sr_added_at` | OLAP 기간 필터 (`added_at` 또는 스케줄 생성 시각과 COALESCE) |
| `idx_restaurants_latlng_gist` | **PostGIS** bbox 검색(`ST_Contains`·envelope vs point) |

공간 인덱스는 **조건식과 동일한 표현식**(`ST_MakePoint(longitude, latitude, …)`)으로 만들어야 인덱스 스캔이 탑니다.

## 4. 트리거와 비정규화

`restaurants.schedule_add_count`는 집계를 빠르게 보여 주기 위한 **중복 저장(denormalization)** 입니다.

- **AFTER INSERT / AFTER DELETE** on `schedule_restaurants` 에서 증감합니다.
- 삭제 시 **`GREATEST(...-1, 0)`** 로 음수 방지.
- PostgreSQL 11~13에서는 `triggers.sql` 안의 **`EXECUTE FUNCTION` 을 `EXECUTE PROCEDURE` 로 교체**해야 할 수 있습니다.

## 5. 애플리케이션과의 관계

- 기본 로컬 설정은 `spring.jpa.hibernate.ddl-auto: update` 로 **엔티티 기준 스키마를 자동 반영**합니다.
- 과제·발표에서는 **`schema.sql`을 단일 진실 공급원(SSOT)** 으로 삼고, 수동 DDL 적용 후 **`spring.profiles.active=dbddl`** 로 `ddl-auto: validate` 만 사용할 수 있습니다 (`application-dbddl.yml`).
  - 수동 DDL과 Hibernate 생성 이름(FK 제약 이름 등)이 1글자라도 다르면 validate 가 실패할 수 있어, 보통은 **하나의 방식(update XOR 순수 DDL)** 을 고릅니다.

## 6. OLAP 쿼리와 스키마 정합성

추천·통계 네이티브 SQL은 **`schedule_restaurants` 조인 `schedules`** 를 전제로 합니다. 레거시 `schedules(user_id, restaurant_id, visit_date_time)` 한 줄 설계와는 호환되지 않습니다.

---

더 시각적인 ERD는 [`erd.md`](erd.md)를 참고하세요.
