# Matzip DB — ERD (현행 정규 스키마)

[`schema.sql`](../src/main/resources/db/schema.sql) 과 동일한 도메인을 기준으로 합니다. 상세 서술은 **[database.md](database.md)** 를 참고하세요.

---

## 1. 관계 다이어그램 (Mermaid)

```mermaid
erDiagram
    users ||--o{ schedules : owns
    users ||--o{ user_preferences : selects
    preferences ||--o{ user_preferences : tagged_by
    schedules ||--o{ schedule_restaurants : contains
    restaurants ||--o{ schedule_restaurants : appears_in

    users {
        bigint id PK
        varchar login_id UK "로그인 아이디"
        varchar password_hash "BCrypt 해시"
        varchar name "실명"
        varchar phone "전화"
        varchar nickname "표시명 optional"
        int age "optional OLAP 연령대"
        timestamp created_at
        timestamp updated_at
    }

    preferences {
        bigint id PK
        varchar code UK "SPICY_HIGH 등"
        varchar display_name
    }

    user_preferences {
        bigint user_id PK_FK
        bigint preference_id PK_FK
    }

    restaurants {
        bigint id PK
        varchar api_id UK "외부 장소 ID optional"
        varchar name
        varchar category
        varchar address
        varchar road_address
        varchar phone
        varchar description
        double latitude "공간 검색"
        double longitude "공간 검색"
        double rating
        int review_count
        int schedule_add_count "트리거 동기화"
    }

    schedules {
        bigint id PK
        bigint user_id FK
        varchar title
        date travel_date
        timestamptz created_at
    }

    schedule_restaurants {
        bigint id PK
        bigint schedule_id FK
        bigint restaurant_id FK
        int visit_order "동선 순서"
        varchar memo
        timestamptz added_at "분석 시각 기준 optional"
    }
```

---

## 2. 레거시 초안과의 차이

팀 초안에 있던 **`plan` / `plan_items`** 명칭은 구현상 **`schedules` / `schedule_restaurants`** 로 매핑되었습니다.

평면형 **`schedules(user_id, restaurant_id, visit_date_time)`** 스키마는 [`schema_legacy_flat_schedules.sql`](../src/main/resources/db/schema_legacy_flat_schedules.sql) 에 보관되어 있습니다.

---

## 3. 공간 인덱스·마이그레이션

- GIST 인덱스 정의: [`postgis.sql`](../src/main/resources/db/postgis.sql)
- `ddl-auto: update` 만 사용하면 **삭제된 컬럼이 DB에 남을 수 있음** → 과제 제출 전 `schema.sql` 과 실제 DB를 한 번 비교하는 것을 권장합니다.
