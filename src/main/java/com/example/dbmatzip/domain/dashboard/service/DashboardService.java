package com.example.dbmatzip.domain.dashboard.service;

import com.example.dbmatzip.domain.analytics.dto.TopRestaurantResponse;
import com.example.dbmatzip.domain.analytics.service.ScheduleAnalyticsService;
import com.example.dbmatzip.domain.dashboard.dto.DashboardStatsResponse;
import com.example.dbmatzip.domain.preference.repository.UserPreferenceRepository;
import com.example.dbmatzip.domain.schedule.repository.ScheduleRepository;
import com.example.dbmatzip.domain.schedule.repository.ScheduleRestaurantRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int DASHBOARD_TOP_RESTAURANTS_LIMIT = 5;

    private final ScheduleRepository scheduleRepository;
    private final ScheduleRestaurantRepository scheduleRestaurantRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final ScheduleAnalyticsService scheduleAnalyticsService;

    public DashboardStatsResponse getMyStats(Long userId) {
        long scheduleCount = scheduleRepository.countByUserId(userId);
        long scheduleItemCount = scheduleRestaurantRepository.countBySchedule_UserId(userId);
        long preferenceCount = userPreferenceRepository.countByUser_Id(userId);
        long recentScheduleItemCount30d = scheduleRestaurantRepository.countRecentAddedItemsByUserId(userId);
        List<TopRestaurantResponse> topRestaurants = scheduleAnalyticsService
                .getTopRestaurantsBySimilarUsers(userId)
                .stream()
                .limit(DASHBOARD_TOP_RESTAURANTS_LIMIT)
                .toList();

        return new DashboardStatsResponse(
                userId, scheduleCount, scheduleItemCount, preferenceCount, recentScheduleItemCount30d, topRestaurants);
    }
}
