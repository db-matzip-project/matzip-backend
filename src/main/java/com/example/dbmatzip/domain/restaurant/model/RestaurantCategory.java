package com.example.dbmatzip.domain.restaurant.model;

import java.util.List;
import java.util.Set;

/**
 * DB 및 {@code GET /api/v1/restaurants?category=} 에서 쓰는 식당 카테고리. 카카오 원시 코드(CE7, FD6… )는 저장 시점에 여기 라벨로만 정규화합니다.
 */
public final class RestaurantCategory {

    /** 프론트 필터 및 API 검색 값과 동일한 문자열 */
    public static final String KOREAN = "한식";
    public static final String JAPANESE = "일식";
    public static final String CHINESE = "중식";
    public static final String WESTERN = "양식";
    public static final String VEGETARIAN = "채식";
    public static final String DESSERT = "디저트";

    public static final List<String> ALLOWED_SEARCH_VALUES =
            List.of(KOREAN, JAPANESE, CHINESE, WESTERN, VEGETARIAN, DESSERT);

    /** {@link #ALLOWED_SEARCH_VALUES} 와 동일 집합 (검색 파라미터 검증 등). */
    public static final Set<String> ALLOWED_LABELS = Set.copyOf(ALLOWED_SEARCH_VALUES);

    private RestaurantCategory() {}
}
