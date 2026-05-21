package com.example.dbmatzip.domain.restaurant.dto;

import com.example.dbmatzip.domain.restaurant.entity.Restaurant;

public record RestaurantResponse(
        Long id,
        String apiId,
        String name,
        String category,
        String address,
        String roadAddress,
        String phone,
        String description,
        double latitude,
        double longitude,
        Double rating,
        Integer reviewCount,
        int scheduleAddCount) {

    public static RestaurantResponse from(Restaurant entity) {
        if (entity.getLatitude() == null || entity.getLongitude() == null) {
            throw new IllegalStateException("식당 위치 정보가 없습니다. id=" + entity.getId());
        }
        int pick = entity.getScheduleAddCount() == null ? 0 : entity.getScheduleAddCount();
        return new RestaurantResponse(
                entity.getId(),
                entity.getApiId(),
                entity.getName(),
                entity.getCategory(),
                entity.getAddress(),
                entity.getRoadAddress(),
                entity.getPhone(),
                entity.getDescription(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getRating(),
                entity.getReviewCount(),
                pick);
    }
}
