package com.example.dbmatzip.domain.schedule.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** 현재 일정에 포함된 {@code restaurants.id} 를 새 방문 순서대로 나열. */
public record ReorderScheduleItemsRequest(@NotEmpty List<Long> orderedRestaurantIds) {}
