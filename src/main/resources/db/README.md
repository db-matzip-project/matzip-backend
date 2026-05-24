# `src/main/resources/db/` 스크립트 안내

| 파일 | 설명 |
|------|------|
| **`schema.sql`** | 과제 기준 **정규 DDL**(테이블·FK·CHECK·B-tree 인덱스). |
| **`postgis.sql`** | PostGIS 확장 + 식당 좌표 **GIST** (`dev-postgis.sql` 내용 통합). |
| **`triggers.sql`** | `schedule_restaurants` INSERT/DELETE 시 `schedule_add_count` 증감 (`schedule-restaurant-trigger.sql` 통합). |
| `sample-data.sql` | 데모용 INSERT (빈 DB에 1회 권장). |
| `normalize-restaurant-categories.sql` | 기존 `restaurants.category` 값을 API·프론트 공통 카테고리(한식/일식/중식/양식/채식/디저트)로 백필. 매칭 실패 행은 `한식` 으로 둠. |
| `schema_legacy_flat_schedules.sql` | 예전 평면 일정 스키마·레포트 비교용. |
| `dev-postgis.sql` | → `postgis.sql` 로 안내하는 스텁. |
| `schedule-restaurant-trigger.sql` | → `triggers.sql` 로 안내하는 스텁. |
| `fix-schedule-add-count-nulls.sql` | Hibernate 마이그레이션 꼬임 시 수동 보정용(예외 상황). |

설계 서술은 [`docs/database.md`](../../../docs/database.md) 를 보세요.
