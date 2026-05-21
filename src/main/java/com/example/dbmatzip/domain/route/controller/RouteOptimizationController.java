package com.example.dbmatzip.domain.route.controller;

import com.example.dbmatzip.domain.route.dto.OptimalOrderRequest;
import com.example.dbmatzip.domain.route.dto.SuggestedVisitOrderResponse;
import com.example.dbmatzip.domain.route.service.RouteOptimizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/route")
@RequiredArgsConstructor
public class RouteOptimizationController {

    private final RouteOptimizationService routeOptimizationService;

    /** 좌표만 알 때 순서 제안 — 로그인 필요 */
    @PostMapping("/optimal-order")
    public SuggestedVisitOrderResponse optimalOrder(@Valid @RequestBody OptimalOrderRequest request) {
        return routeOptimizationService.suggestOrder(request.restaurantIds(), request.startRestaurantId());
    }
}
