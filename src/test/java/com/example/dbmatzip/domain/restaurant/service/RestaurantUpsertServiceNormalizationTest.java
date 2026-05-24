package com.example.dbmatzip.domain.restaurant.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.dbmatzip.domain.restaurant.model.RestaurantCategory;
import org.junit.jupiter.api.Test;

class RestaurantUpsertServiceNormalizationTest {

    @Test
    void koreanFoodPath_with_fd6() {
        assertThat(RestaurantUpsertService.normalizeCategory("음식점 > 한식 > 국밥", "음식점", "FD6"))
                .isEqualTo(RestaurantCategory.KOREAN);
    }

    @Test
    void cafe_via_ce7_in_group_field_or_standalone_category_name() {
        assertThat(RestaurantUpsertService.normalizeCategory(null, null, "CE7"))
                .isEqualTo(RestaurantCategory.DESSERT);
        assertThat(RestaurantUpsertService.normalizeCategory("CE7", null, null))
                .isEqualTo(RestaurantCategory.DESSERT);
        assertThat(RestaurantUpsertService.normalizeCategory("ce7", null, null))
                .isEqualTo(RestaurantCategory.DESSERT);
    }

    @Test
    void japanese_keyword_with_prefix() {
        assertThat(RestaurantUpsertService.normalizeCategory("음식점 > 일식 > 초밥", "음식점", "FD6"))
                .isEqualTo(RestaurantCategory.JAPANESE);
    }

    @Test
    void cafe_inside_fd6_name_is_dessert() {
        assertThat(RestaurantUpsertService.normalizeCategory("음식점 > 카페", "음식점", "FD6"))
                .isEqualTo(RestaurantCategory.DESSERT);
    }

    @Test
    void fd6_only_fallback_korean() {
        assertThat(RestaurantUpsertService.normalizeCategory("FD6", null, null))
                .isEqualTo(RestaurantCategory.KOREAN);
    }

    @Test
    void unknown_non_food_group_goes_to_western_placeholder() {
        assertThat(RestaurantUpsertService.normalizeCategory(null, null, "MT1"))
                .isEqualTo(RestaurantCategory.WESTERN);
    }

    @Test
    void ambiguous_legacy_inputs_still_inside_allowed_enums() {
        assertThat(RestaurantCategory.ALLOWED_LABELS).contains(RestaurantUpsertService.normalizeCategory("", "", ""));
        assertThat(RestaurantCategory.ALLOWED_LABELS)
                .contains(RestaurantUpsertService.normalizeCategory("x", "", ""));
        assertThat(RestaurantCategory.ALLOWED_LABELS)
                .contains(RestaurantUpsertService.normalizeCategory("", "음식점", ""));
    }
}
