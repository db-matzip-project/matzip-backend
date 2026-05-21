# matzip-backend

## Backend & DB scope

This repository now includes the normalized core schema for:

- `users`
- `preferences`
- `user_preferences` (N:M mapping table)
- `restaurants`
- `schedules`

로컬 실행 설정은 **`src/main/resources/application.yml` 하나만** 사용합니다 (`application.properties`와 동시에 두면 오류 원인이 됩니다).

- **JWT**: 운영에서는 `JWT_SECRET` 환경변수로 `jwt.secret` 을 반드시 설정하세요 (충분히 긴 문자열).
- 인증이 필요한 API는 헤더 `Authorization: Bearer <accessToken>` 을 붙입니다.

## 인증 · 회원 · 취향 API

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/v1/auth/signup` | 회원가입 (이름·아이디·비번·전화 등), 응답에 JWT 포함 |
| POST | `/api/v1/auth/login` | 로그인 |
| POST | `/api/v1/auth/logout` | 무상태 JWT — 클라이언트에서 토큰 폐기용 noop |
| GET/PATCH | `/api/v1/users/me` | 내 정보 조회·수정 |
| GET | `/api/v1/preferences` | 온보딩용 취향 태그 전체 |
| GET/PUT | `/api/v1/preferences/me` | 내 취향 조회·일괄 교체 |

## 식당 · 일정 · 동선 API (요약)

- `GET /api/v1/restaurants` — 공개. `tasteSimilar=true` 이면 **로그인 필수**, 입맛 비슷한 사용자 추천 ID ∩ bounds 검색.
- `POST /api/v1/restaurants/import/kakao` — 로그인 필요.
- `/api/v1/schedules/**` — 일정 CRUD·항목·순서 변경은 **JWT 필수** (쿼리 `userId` 제거, 토큰의 사용자 기준).
- `GET /api/v1/schedules/{id}/route/legs` — 저장된 순서 기준 구간 거리(km).
- `GET /api/v1/schedules/{id}/route/suggested-order` — 근사 최단 방문 순서 제안 (DB 미반영).
- `POST /api/v1/route/optimal-order` — 식당 ID 목록만으로 순서 제안.

**DB 과제 제출 기준 DDL**: `src/main/resources/db/schema.sql` → 이어서 `postgis.sql`, `triggers.sql` 순 실행. 요약·인덱스·트리거 논리는 **[docs/database.md](docs/database.md)** 참고.

레거시 평면 일정 스키마는 `src/main/resources/db/schema_legacy_flat_schedules.sql` 에 보관했습니다.

## OLAP query implementation

The requirement:

> "Users similar to me (for example, same spicy-food preference and same age group) and their top 10 restaurants by schedule registrations in the last month."

**구현 참고:** 저장 스키마는 `schedule_restaurants` 이며, 분석 기간은 코드 상 **최근 3개월**과 `COALESCE(added_at, schedules.created_at)` 기준입니다 (교과서 문구와 단위만 다를 수 있음).

는 **`schedule_restaurants` + `schedules`** 스키마 기준 네이티브 SQL로 구현되어 있습니다.

- 최근 활동: `COALESCE(schedule_restaurants.added_at, schedules.created_at)` 기준 **최근 3개월**
- 유사 사용자: **나이대(10세 단위, 둘 다 age 가 있을 때만 매칭)** + **내가 고른 취향 태그와 겹치는 개수 ≥ min(3, 내 태그 개수)**

구현 위치:

- `src/main/java/com/example/dbmatzip/domain/analytics/repository/ScheduleAnalyticsRepository.java`

Method:

- `findTop10RestaurantsBySimilarUsers(Long userId)`
- `findRecommendedRestaurantIds(Long userId, int limit)` — 지도 검색 `tasteSimilar` 필터용

Returns:

- `restaurantId`
- `restaurantName`
- `scheduleCount`
- `contributorUserCount`

through projection interface:

- `src/main/java/com/example/dbmatzip/domain/analytics/dto/SimilarTasteRestaurantStat.java`

## API endpoint

- `GET /api/v1/analytics/similar-users/top-restaurants` (Bearer JWT)
- Controller: `src/main/java/com/example/dbmatzip/domain/analytics/controller/ScheduleAnalyticsController.java`

## Sample data & Postman

- 스크립트 안내: `src/main/resources/db/README.md`
- 데모 INSERT: `src/main/resources/db/sample-data.sql` (빈 DB에 1회 권장; 중복 실행 시 일정 행이 늘어날 수 있음)
- Postman: `docs/postman-analytics-example.md`

DDL 적용 후 엔티티만 검증하고 싶으면 프로파일 **`dbddl`** (`application-dbddl.yml`: `ddl-auto: validate`)를 사용합니다.