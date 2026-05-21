package com.example.dbmatzip.domain.route.dto;

import java.util.List;

public record SuggestedVisitOrderResponse(List<Long> restaurantIdsInVisitOrder, double totalDistanceKm) {}
