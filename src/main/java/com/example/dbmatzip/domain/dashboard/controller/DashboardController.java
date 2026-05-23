package com.example.dbmatzip.domain.dashboard.controller;

import com.example.dbmatzip.domain.dashboard.dto.DashboardStatsResponse;
import com.example.dbmatzip.domain.dashboard.service.DashboardService;
import com.example.dbmatzip.global.security.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping({"/me", "/stats", "/stats/me"})
    @Operation(summary = "내 대시보드 통계", description = "내 일정/취향 카운트와 유사 취향 기반 추천 식당 요약을 반환합니다.")
    public DashboardStatsResponse myDashboard(@AuthenticationPrincipal MemberPrincipal principal) {
        return dashboardService.getMyStats(principal.getId());
    }
}
