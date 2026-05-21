# Postman request example

## 0) 로그인 후 토큰 받기

1. `POST http://localhost:8081/api/v1/auth/signup` 또는 `POST .../auth/login`
2. 응답의 `accessToken` 값을 복사합니다.

## 1) 입맛 비슷한 사용자 TOP 식당 (로그인 필요)

- Method: `GET`
- URL: `http://localhost:8081/api/v1/analytics/similar-users/top-restaurants`
- Headers: `Authorization: Bearer <accessToken>`

Expected response example:

```json
[
  {
    "restaurantId": 1,
    "restaurantName": "청양칼국수",
    "scheduleCount": 3,
    "contributorUserCount": 3
  },
  {
    "restaurantId": 2,
    "restaurantName": "홍염떡볶이",
    "scheduleCount": 2,
    "contributorUserCount": 2
  }
]
```

## 2) Demo data insertion (manual)

Run SQL manually before API test:

- 실행 순서: `schema.sql` → `postgis.sql` → `triggers.sql` → (선택) `sample-data.sql`
- 디렉터리 안내: `src/main/resources/db/README.md`

You can execute scripts in pgAdmin or psql.

Note: 최신 도메인은 `schedules` + `schedule_restaurants` 형태입니다.
