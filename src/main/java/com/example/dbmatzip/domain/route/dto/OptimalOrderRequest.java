package com.example.dbmatzip.domain.route.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OptimalOrderRequest(
        @NotEmpty @Size(max = 50) List<Long> restaurantIds,
        Long startRestaurantId) {}
