package com.example.dbmatzip.domain.schedule.controller;

import com.example.dbmatzip.domain.route.dto.RouteLegResponse;
import com.example.dbmatzip.domain.route.dto.SuggestedVisitOrderResponse;
import com.example.dbmatzip.domain.route.service.RouteOptimizationService;
import com.example.dbmatzip.global.security.MemberPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/schedules/{scheduleId}/route")
@RequiredArgsConstructor
public class ScheduleRouteController {

    private final RouteOptimizationService routeOptimizationService;

    /** 현재 저장된 방문 순서대로 구간별 직선 거리(km). 프론트 지도 Polyline 과 별개 */
    @GetMapping("/legs")
    public List<RouteLegResponse> legs(
            @PathVariable Long scheduleId, @AuthenticationPrincipal MemberPrincipal principal) {
        return routeOptimizationService.legsForSchedule(scheduleId, principal.getId());
    }

    /** 근사 최단 순서 제안(DB 순서는 변경하지 않음 — 적용 시 PUT …/items/order 사용) */
    @GetMapping("/suggested-order")
    public SuggestedVisitOrderResponse suggestedOrder(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestParam(required = false) Long startRestaurantId) {
        return routeOptimizationService.suggestOrderForSchedule(scheduleId, principal.getId(), startRestaurantId);
    }
}
