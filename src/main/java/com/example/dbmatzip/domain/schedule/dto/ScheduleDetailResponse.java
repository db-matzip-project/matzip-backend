package com.example.dbmatzip.domain.schedule.dto;

import com.example.dbmatzip.domain.restaurant.dto.RestaurantResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ScheduleDetailResponse(
        Long id,
        String title,
        LocalDate travelDate,
        Instant createdAt,
        List<ScheduleItemResponse> items) {

    public record ScheduleItemResponse(
            Long itemId, int visitOrder, String memo, RestaurantResponse restaurant) {}
}
