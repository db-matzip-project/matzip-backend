package com.example.dbmatzip.domain.review.dto;

import com.example.dbmatzip.domain.review.entity.Review;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long restaurantId,
        Long userId,
        String userName,
        String content,
        Integer rating,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        /** 마이페이지 등에서만 채워집니다. 식당 상세 리뷰 목록에서는 생략됩니다. */
        @JsonInclude(JsonInclude.Include.NON_NULL) String restaurantName) {

    public static ReviewResponse from(Review review) {
        return from(review, null);
    }

    public static ReviewResponse from(Review review, String restaurantName) {
        return new ReviewResponse(
                review.getId(),
                review.getRestaurant().getId(),
                review.getUser().getId(),
                review.getUser().getName(),
                review.getContent(),
                review.getRating(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                restaurantName);
    }
}
