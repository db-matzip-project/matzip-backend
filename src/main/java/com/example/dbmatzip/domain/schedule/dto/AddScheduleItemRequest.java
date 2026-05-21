package com.example.dbmatzip.domain.schedule.dto;

import jakarta.validation.constraints.NotNull;

public record AddScheduleItemRequest(@NotNull Long restaurantId, String memo) {}
