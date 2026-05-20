# Postman request example

## 1) Top restaurants from similar users

- Method: `GET`
- URL: `http://localhost:8080/api/v1/analytics/similar-users/top-restaurants?userId=1`

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

- File: `src/main/resources/db/sample-data.sql`

You can execute it in pgAdmin or psql after `schema.sql` is applied.
