package com.example.dbmatzip.domain.restaurant.dto;

import com.example.dbmatzip.domain.restaurant.entity.Restaurant;
import io.swagger.v3.oas.annotations.media.Schema;

public record RestaurantResponse(
        Long id,
        String apiId,
        String name,
        String category,
        String address,
        String roadAddress,
        String phone,
        String description,
        @Schema(description = "위도. 값이 없으면 0.0", example = "37.5665")
        double latitude,
        @Schema(description = "경도. 값이 없으면 0.0", example = "126.9780")
        double longitude,
        @Schema(description = "평점. 값이 없으면 0.0", example = "4.5")
        double rating,
        @Schema(description = "리뷰 수. 값이 없으면 0", example = "120")
        int reviewCount,
        int scheduleAddCount) {

    public static RestaurantResponse from(Restaurant entity) {
        int pick = valueOrZero(entity.getScheduleAddCount());
        return new RestaurantResponse(
                entity.getId(),
                entity.getApiId(),
                nullToEmpty(entity.getName()),
                nullToEmpty(entity.getCategory()),
                nullToEmpty(entity.getAddress()),
                nullToEmpty(entity.getRoadAddress()),
                nullToEmpty(entity.getPhone()),
                nullToEmpty(entity.getDescription()),
                valueOrZero(entity.getLatitude()),
                valueOrZero(entity.getLongitude()),
                valueOrZero(entity.getRating()),
                valueOrZero(entity.getReviewCount()),
                pick);
    }

    private static double valueOrZero(Double value) {
        return value == null ? 0.0 : value;
    }

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
