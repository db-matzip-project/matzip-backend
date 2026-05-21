package com.example.dbmatzip.domain.analytics.controller;

import com.example.dbmatzip.domain.analytics.dto.TopRestaurantResponse;
import com.example.dbmatzip.domain.analytics.service.ScheduleAnalyticsService;
import com.example.dbmatzip.global.security.MemberPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/analytics")
public class ScheduleAnalyticsController {

    private final ScheduleAnalyticsService scheduleAnalyticsService;

    /** 로그인 사용자 기준 입맛 비슷한 사용자들의 최근 일정 데이터 TOP 식당 */
    @GetMapping("/similar-users/top-restaurants")
    public List<TopRestaurantResponse> getTopRestaurants(@AuthenticationPrincipal MemberPrincipal principal) {
        return scheduleAnalyticsService.getTopRestaurantsBySimilarUsers(principal.getId());
    }
}
