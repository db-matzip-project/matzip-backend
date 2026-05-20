package com.example.dbmatzip.domain.analytics.dto;

public record TopRestaurantResponse(
        Long restaurantId,
        String restaurantName,
        Long scheduleCount,
        Long contributorUserCount
) {

    public static TopRestaurantResponse from(SimilarTasteRestaurantStat stat) {
        return new TopRestaurantResponse(
                stat.getRestaurantId(),
                stat.getRestaurantName(),
                stat.getScheduleCount(),
                stat.getContributorUserCount()
        );
    }
}
