package com.example.dbmatzip.domain.analytics.controller;

import com.example.dbmatzip.domain.analytics.dto.TopRestaurantResponse;
import com.example.dbmatzip.domain.analytics.service.ScheduleAnalyticsService;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/analytics")
public class ScheduleAnalyticsController {

    private final ScheduleAnalyticsService scheduleAnalyticsService;

    @GetMapping("/similar-users/top-restaurants")
    public List<TopRestaurantResponse> getTopRestaurants(
            @RequestParam @Min(1) Long userId
    ) {
        return scheduleAnalyticsService.getTopRestaurantsBySimilarUsers(userId);
    }
}
