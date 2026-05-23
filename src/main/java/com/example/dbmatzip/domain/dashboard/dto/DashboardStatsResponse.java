package com.example.dbmatzip.domain.dashboard.dto;

import com.example.dbmatzip.domain.analytics.dto.TopRestaurantResponse;
import java.util.List;

public record DashboardStatsResponse(
        Long userId,
        long scheduleCount,
        long scheduleItemCount,
        long preferenceCount,
        long recentScheduleItemCount30d,
        List<TopRestaurantResponse> similarTasteTopRestaurants) {}
