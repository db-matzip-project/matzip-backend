# matzip-backend

## Backend & DB scope

This repository now includes the normalized core schema for:

- `users`
- `preferences`
- `user_preferences` (N:M mapping table)
- `restaurants`
- `schedules`

로컬 실행 설정은 **`src/main/resources/application.yml` 하나만** 사용합니다 (`application.properties`와 동시에 두면 오류 원인이 됩니다).

- **JWT**: `JWT_SECRET` 환경변수는 개발/운영 모두 필수입니다 (최소 32자 이상).
- 인증이 필요한 API는 헤더 `Authorization: Bearer <accessToken>` 을 붙입니다.
  (하위호환을 위해 raw 토큰 문자열도 허용하지만, 프론트는 `Bearer` 형식 사용을 권장합니다.)

## CORS / Auth 호출 규칙 (프론트 연동)

- 허용 Origin: `http://localhost:5173`, `http://localhost:5174` (로컬 개발용)
- 허용 Method: `GET, POST, PUT, PATCH, DELETE, OPTIONS`
- 허용 Header: `Authorization`, `Content-Type` 등
- `POST /api/v1/auth/signup`, `POST /api/v1/auth/login`, `POST /api/v1/auth/logout` 은 `permitAll`
- `logout`은 JWT 무상태 구조로 서버 측에서는 noop이며, 인증 헤더 없이도 호출 가능합니다.
- 보안 에러 응답 형식: `{"code":"...","message":"..."}`
  - 미인증: `401 UNAUTHORIZED`
  - 권한 없음: `403 FORBIDDEN`

## Swagger UI (API 검증)

앱 기동 후 브라우저에서 **`http://localhost:8080/swagger-ui.html`** 을 엽니다.  
로그인/회원가입 등으로 받은 `accessToken`을 상단 **Authorize** 에 넣으면 보호된 엔드포인트를 같은 탭에서 호출할 수 있습니다. (HTTP Bearer 형식이라 `Bearer ` 접두어는 UI가 붙입니다.)

## 인증 · 회원 · 취향 API

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/v1/auth/signup` | 회원가입 (이름·아이디·비번·전화 등), 응답에 JWT 포함. `preferenceIds`를 함께 보내면 `user_preferences`도 즉시 저장 |
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

`GET /api/v1/restaurants` 파라미터 규칙:

- `category`: `한식`, `일식`, `중식`, `양식`, `채식`, `디저트` 권장. (정확 일치 + category/description 부분 일치 검색)
- `minRating`: 최소 평점 필터 (`4.0`, `4.5` 등)
- `sortBy`(권장): `rating`(평점 높은순), `rating_asc`, `reviews`(리뷰 많은순), `review_count_asc`, `distance`
- `sort`(하위호환): 기존 파라미터도 계속 지원

## 리뷰 API (프론트 연동용)

리뷰/평점은 이제 웹 사용자가 입력한 값이 `reviews` 테이블에 저장되고, 저장/삭제 시 식당 집계값(`restaurants.rating`, `restaurants.review_count`)도 즉시 갱신됩니다.

- `POST /api/v1/restaurants/{restaurantId}/reviews` (Bearer JWT)
  - 설명: 리뷰 작성/수정(upsert). 같은 사용자가 같은 식당에 다시 작성하면 수정됩니다.
  - 요청:

```json
{
  "content": "맛있고 재방문 의사 있어요",
  "rating": 5
}
```

  - 성공 응답(201):

```json
{
  "id": 12,
  "restaurantId": 3,
  "userId": 7,
  "content": "맛있고 재방문 의사 있어요",
  "rating": 5,
  "createdAt": "2026-05-24T16:28:31.102",
  "updatedAt": "2026-05-24T16:28:31.102"
}
```

- `GET /api/v1/restaurants/{restaurantId}/reviews?page=0&size=20` (공개)
  - 설명: 식당 리뷰 목록 조회(페이지네이션)
  - 성공 응답: 기존 `PageResponse<T>` 포맷

- `DELETE /api/v1/restaurants/{restaurantId}/reviews/{reviewId}` (Bearer JWT)
  - 설명: 본인 리뷰만 삭제 가능
  - 성공 응답: `204 No Content`

주요 에러 코드:

- `400 BAD_REQUEST`: rating 범위(1~5) 위반, 식당 ID 불일치 등
- `401 UNAUTHORIZED`: 토큰 누락/만료
- `403 FORBIDDEN`: 본인 리뷰가 아닌 삭제 시도
- `404 NOT_FOUND`: 식당 또는 리뷰 없음

### 일정 생성 시 매핑 데이터 적재

`POST /api/v1/schedules` 요청 본문에 `restaurantIds`를 함께 보내면, 일정 헤더 생성과 동시에
`schedule_restaurants` 매핑 행이 방문 순서(`visit_order`) 1부터 자동 저장됩니다.

- `restaurantIds`는 선택 필드입니다.
- 미전달/빈 배열이면 일정만 생성되고, 식당은 `POST /api/v1/schedules/{scheduleId}/items`로 추가합니다.
- 중복 ID가 포함되면 `400 BAD_REQUEST`를 반환합니다.

예시:

```json
{
  "title": "주말 코스",
  "travelDate": "2026-05-24",
  "restaurantIds": [1, 3, 5]
}
```

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
- `GET /api/v1/analytics/similar-users/top-restaurants/me` (Bearer JWT, alias)
- Controller: `src/main/java/com/example/dbmatzip/domain/analytics/controller/ScheduleAnalyticsController.java`

## Dashboard stats API

- `GET /api/v1/dashboard/me` (Bearer JWT)
- `GET /api/v1/dashboard/stats` (Bearer JWT, alias)
- 반환값에는 내 일정 수, 일정-식당 매핑 수, 취향 수, 최근 30일 일정 아이템 수, 유사취향 TOP 추천 일부가 포함됩니다.

## Sample data & Postman

- 스크립트 안내: `src/main/resources/db/README.md`
- 데모 INSERT: `src/main/resources/db/sample-data.sql` (빈 DB에 1회 권장; 중복 실행 시 일정 행이 늘어날 수 있음)
- Postman: `docs/postman-analytics-example.md`
- Postman (dashboard): `docs/postman-dashboard-example.md`
- Postman (member flow): `docs/postman-member-flow-example.md`
- Postman collection JSON: `docs/postman-member3-collection.json` (Import 후 바로 실행 가능)

DDL 적용 후 엔티티만 검증하고 싶으면 프로파일 **`dbddl`** (`application-dbddl.yml`: `ddl-auto: validate`)를 사용합니다.