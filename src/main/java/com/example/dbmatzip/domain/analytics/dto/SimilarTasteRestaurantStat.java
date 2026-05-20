package com.example.dbmatzip.domain.analytics.dto;

public interface SimilarTasteRestaurantStat {

    Long getRestaurantId();

    String getRestaurantName();

    Long getScheduleCount();

    Long getContributorUserCount();
}
