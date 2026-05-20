# matzip-backend

## Backend & DB scope

This repository now includes the normalized core schema for:

- `users`
- `preferences`
- `user_preferences` (N:M mapping table)
- `restaurants`
- `schedules`

Schema SQL: `src/main/resources/db/schema.sql`

## OLAP query implementation

The requirement:

> "Users similar to me (for example, same spicy-food preference and same age group) and their top 10 restaurants by schedule registrations in the last month."

is implemented as a native query in:

- `src/main/java/com/example/dbmatzip/domain/analytics/repository/ScheduleAnalyticsRepository.java`

Method:

- `findTop10RestaurantsBySimilarUsers(Long userId)`

Returns:

- `restaurantId`
- `restaurantName`
- `scheduleCount`
- `contributorUserCount`

through projection interface:

- `src/main/java/com/example/dbmatzip/domain/analytics/dto/SimilarTasteRestaurantStat.java`

## API endpoint

- `GET /api/v1/analytics/similar-users/top-restaurants?userId={id}`
- Controller: `src/main/java/com/example/dbmatzip/domain/analytics/controller/ScheduleAnalyticsController.java`

## Sample data & Postman

- Sample SQL: `src/main/resources/db/sample-data.sql` (manual execution)
- Postman request guide: `docs/postman-analytics-example.md`