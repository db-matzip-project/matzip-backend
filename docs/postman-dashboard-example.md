# Postman dashboard API example

## 0) 로그인 후 토큰 받기

1. `POST http://localhost:8080/api/v1/auth/login`
2. 응답의 `accessToken` 값을 복사합니다.

## 1) 내 대시보드 통계 조회 (JWT 필요)

- Method: `GET`
- URL: `http://localhost:8080/api/v1/dashboard/me`
- Headers: `Authorization: Bearer <accessToken>`

Alias 경로도 동일 응답:

- `GET /api/v1/dashboard/stats`
- `GET /api/v1/dashboard/stats/me`

Expected response example:

```json
{
  "userId": 1,
  "scheduleCount": 3,
  "scheduleItemCount": 8,
  "preferenceCount": 4,
  "recentScheduleItemCount30d": 5,
  "similarTasteTopRestaurants": [
    {
      "restaurantId": 10,
      "restaurantName": "청양칼국수",
      "scheduleCount": 3,
      "contributorUserCount": 2
    }
  ]
}
```

## 2) 데이터가 비어 보일 때 점검 SQL

```sql
SELECT COUNT(*) FROM schedules WHERE user_id = <myUserId>;
SELECT COUNT(*) FROM schedule_restaurants sr
JOIN schedules s ON s.id = sr.schedule_id
WHERE s.user_id = <myUserId>;
SELECT COUNT(*) FROM user_preferences WHERE user_id = <myUserId>;
```
