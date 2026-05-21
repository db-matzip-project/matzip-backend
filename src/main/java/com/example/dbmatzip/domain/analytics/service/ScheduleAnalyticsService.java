package com.example.dbmatzip.domain.analytics.service;

import com.example.dbmatzip.domain.analytics.dto.TopRestaurantResponse;
import com.example.dbmatzip.domain.analytics.repository.ScheduleAnalyticsRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleAnalyticsService {

    private final ScheduleAnalyticsRepository scheduleAnalyticsRepository;

    public List<TopRestaurantResponse> getTopRestaurantsBySimilarUsers(Long userId) {
        return scheduleAnalyticsRepository.findTop10RestaurantsBySimilarUsers(userId)
                .stream()
                .map(TopRestaurantResponse::from)
                .toList();
    }
}
