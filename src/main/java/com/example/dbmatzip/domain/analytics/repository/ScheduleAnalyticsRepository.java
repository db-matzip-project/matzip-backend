package com.example.dbmatzip.domain.analytics.repository;

import com.example.dbmatzip.domain.analytics.dto.SimilarTasteRestaurantStat;
import com.example.dbmatzip.domain.schedule.entity.Schedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleAnalyticsRepository extends JpaRepository<Schedule, Long> {

    @Query(value = """
            WITH target_user AS (
                SELECT u.id, u.age
                FROM users u
                WHERE u.id = :userId
            ),
            target_spicy_pref AS (
                SELECT up.preference_id
                FROM user_preferences up
                JOIN preferences p ON p.id = up.preference_id
                WHERE up.user_id = :userId
                  AND p.code LIKE 'SPICY_%'
            ),
            similar_users AS (
                SELECT u2.id AS user_id
                FROM users u2
                WHERE u2.id <> :userId
                  AND (u2.age / 10) = (SELECT age / 10 FROM target_user)
                  AND EXISTS (
                      SELECT 1
                      FROM user_preferences up2
                      JOIN target_spicy_pref tsp ON tsp.preference_id = up2.preference_id
                      WHERE up2.user_id = u2.id
                  )
            ),
            recent_schedules AS (
                SELECT s.user_id, s.restaurant_id
                FROM schedules s
                WHERE s.visit_date_time >= NOW() - INTERVAL '1 month'
            )
            SELECT
                r.id AS restaurantId,
                r.name AS restaurantName,
                COUNT(*) AS scheduleCount,
                COUNT(DISTINCT rs.user_id) AS contributorUserCount
            FROM recent_schedules rs
            JOIN similar_users su ON su.user_id = rs.user_id
            JOIN restaurants r ON r.id = rs.restaurant_id
            GROUP BY r.id, r.name
            ORDER BY scheduleCount DESC, contributorUserCount DESC, r.id ASC
            LIMIT 10
            """, nativeQuery = true)
    List<SimilarTasteRestaurantStat> findTop10RestaurantsBySimilarUsers(@Param("userId") Long userId);
}
