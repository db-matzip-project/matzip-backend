package com.example.dbmatzip.domain.review.entity;

import com.example.dbmatzip.domain.member.entity.User;
import com.example.dbmatzip.domain.restaurant.entity.Restaurant;
import com.example.dbmatzip.global.entity.BaseTimeEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자-식당 리뷰.
 * 기본 정책: 사용자당 식당 1개 리뷰(수정 가능).
 */
@Entity
@Table(
        name = "reviews",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uq_reviews_restaurant_user",
                        columnNames = {"restaurant_id", "user_id"}),
        indexes = {
            @Index(name = "idx_reviews_restaurant_created_at", columnList = "restaurant_id, created_at"),
            @Index(name = "idx_reviews_user_created_at", columnList = "user_id, created_at"),
            @Index(name = "idx_reviews_restaurant_rating", columnList = "restaurant_id, rating")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Access(AccessType.FIELD)
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private Integer rating;

    private Review(Restaurant restaurant, User user, String content, Integer rating) {
        this.restaurant = restaurant;
        this.user = user;
        this.content = normalizeContent(content);
        this.rating = validateRating(rating);
    }

    public static Review create(Restaurant restaurant, User user, String content, Integer rating) {
        return new Review(restaurant, user, content, rating);
    }

    public void update(String content, Integer rating) {
        this.content = normalizeContent(content);
        this.rating = validateRating(rating);
    }

    private static String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("리뷰 내용은 공백일 수 없습니다.");
        }
        String trimmed = content.trim();
        if (trimmed.length() > 1000) {
            throw new IllegalArgumentException("리뷰 내용은 1000자 이하여야 합니다.");
        }
        return trimmed;
    }

    private static Integer validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1~5 사이여야 합니다.");
        }
        return rating;
    }
}
