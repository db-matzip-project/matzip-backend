package com.example.dbmatzip.domain.analytics.repository;

import com.example.dbmatzip.domain.analytics.dto.SimilarTasteRestaurantStat;
import com.example.dbmatzip.domain.schedule.entity.Schedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleAnalyticsRepository extends JpaRepository<Schedule, Long> {

    @Query(
            value =
                    """
                    WITH target_prefs AS (
                        SELECT preference_id FROM user_preferences WHERE user_id = :userId
                    ),
                    thresh AS (
                        SELECT CASE WHEN COUNT(*) = 0 THEN NULL ELSE LEAST(3, COUNT(*)::int) END AS min_overlap
                        FROM target_prefs
                    ),
                    similar_users AS (
                        SELECT u2.id AS uid
                        FROM users u2
                        CROSS JOIN thresh th
                        WHERE th.min_overlap IS NOT NULL
                          AND u2.id <> :userId
                          AND (
                              SELECT COUNT(*)::int FROM user_preferences up2
                              INNER JOIN target_prefs tp ON tp.preference_id = up2.preference_id
                              WHERE up2.user_id = u2.id
                          ) >= th.min_overlap
                          AND (
                              (SELECT age FROM users WHERE id = :userId) IS NULL
                              OR u2.age IS NULL
                              OR ((SELECT age FROM users WHERE id = :userId) / 10) = (u2.age / 10)
                          )
                    ),
                    recent_activity AS (
                        SELECT sr.restaurant_id, s.user_id
                        FROM schedule_restaurants sr
                        INNER JOIN schedules s ON s.id = sr.schedule_id
                        WHERE COALESCE(sr.added_at, s.created_at) >= NOW() - INTERVAL '3 months'
                    )
                    SELECT r.id AS restaurantId,
                           r.name AS restaurantName,
                           COUNT(*) AS scheduleCount,
                           COUNT(DISTINCT ra.user_id) AS contributorUserCount
                    FROM recent_activity ra
                    INNER JOIN similar_users su ON su.uid = ra.user_id
                    INNER JOIN restaurants r ON r.id = ra.restaurant_id
                    GROUP BY r.id, r.name
                    ORDER BY COUNT(*) DESC, COUNT(DISTINCT ra.user_id) DESC, r.id ASC
                    LIMIT 10
                    """,
            nativeQuery = true)
    List<SimilarTasteRestaurantStat> findTop10RestaurantsBySimilarUsers(@Param("userId") Long userId);

    @Query(
            value =
                    """
                    WITH target_prefs AS (
                        SELECT preference_id FROM user_preferences WHERE user_id = :userId
                    ),
                    thresh AS (
                        SELECT CASE WHEN COUNT(*) = 0 THEN NULL ELSE LEAST(3, COUNT(*)::int) END AS min_overlap
                        FROM target_prefs
                    ),
                    similar_users AS (
                        SELECT u2.id AS uid
                        FROM users u2
                        CROSS JOIN thresh th
                        WHERE th.min_overlap IS NOT NULL
                          AND u2.id <> :userId
                          AND (
                              SELECT COUNT(*)::int FROM user_preferences up2
                              INNER JOIN target_prefs tp ON tp.preference_id = up2.preference_id
                              WHERE up2.user_id = u2.id
                          ) >= th.min_overlap
                          AND (
                              (SELECT age FROM users WHERE id = :userId) IS NULL
                              OR u2.age IS NULL
                              OR ((SELECT age FROM users WHERE id = :userId) / 10) = (u2.age / 10)
                          )
                    ),
                    recent_activity AS (
                        SELECT sr.restaurant_id, s.user_id
                        FROM schedule_restaurants sr
                        INNER JOIN schedules s ON s.id = sr.schedule_id
                        WHERE COALESCE(sr.added_at, s.created_at) >= NOW() - INTERVAL '3 months'
                    )
                    SELECT ra.restaurant_id
                    FROM recent_activity ra
                    INNER JOIN similar_users su ON su.uid = ra.user_id
                    GROUP BY ra.restaurant_id
                    ORDER BY COUNT(*) DESC, COUNT(DISTINCT ra.user_id) DESC, ra.restaurant_id ASC
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<Long> findRecommendedRestaurantIds(@Param("userId") Long userId, @Param("limit") int limit);
}
