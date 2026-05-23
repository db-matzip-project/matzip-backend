# Postman member flow example (member 3)

## 1) 취향 목록 조회

- Method: `GET`
- URL: `http://localhost:8080/api/v1/preferences`

응답에서 취향 ID를 2~3개 고릅니다.

## 2) 회원가입 시 취향 동시 저장

- Method: `POST`
- URL: `http://localhost:8080/api/v1/auth/signup`
- Body:

```json
{
  "loginId": "member3_demo",
  "password": "Password123!",
  "name": "홍길동",
  "phone": "01012345678",
  "nickname": "길동이",
  "age": 24,
  "preferenceIds": [1, 3, 5]
}
```

`preferenceIds`를 보내면 `user_preferences`가 즉시 저장됩니다.

## 3) 내 취향 조회

- Method: `GET`
- URL: `http://localhost:8080/api/v1/preferences/me`
- Headers: `Authorization: Bearer <accessToken>`

## 4) DB 검증 SQL

```sql
SELECT id, login_id FROM users WHERE login_id = 'member3_demo';
SELECT * FROM user_preferences
WHERE user_id = (SELECT id FROM users WHERE login_id = 'member3_demo');
```
