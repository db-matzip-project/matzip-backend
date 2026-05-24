package com.example.dbmatzip.domain.schedule.dto;

import com.example.dbmatzip.domain.restaurant.dto.UpsertRestaurantFromPlacePayload;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record AddSchedulePlaceItemRequest(
        String memo,
        /** 일정 추가 시 레스토랑 행 없으면 만들고, 있으면 api_id 기준 정보 갱신 후 연결 */
        @Valid @NotNull UpsertRestaurantFromPlacePayload place) {}
