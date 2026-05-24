package com.example.dbmatzip.domain.review.dto;

import com.example.dbmatzip.domain.review.entity.Review;
import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long restaurantId,
        Long userId,
        String content,
        Integer rating,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getRestaurant().getId(),
                review.getUser().getId(),
                review.getContent(),
                review.getRating(),
                review.getCreatedAt(),
                review.getUpdatedAt());
    }
}
