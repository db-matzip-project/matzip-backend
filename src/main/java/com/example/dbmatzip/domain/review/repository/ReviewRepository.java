package com.example.dbmatzip.domain.review.repository;

import com.example.dbmatzip.domain.review.entity.Review;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByRestaurantIdAndUserId(Long restaurantId, Long userId);

    Optional<Review> findByRestaurantIdAndUserId(Long restaurantId, Long userId);

    Page<Review> findByRestaurantId(Long restaurantId, Pageable pageable);

    /** 마이페이지: 본인 리뷰 + 식당명 로딩(N+1 방지). */
    @EntityGraph(attributePaths = {"restaurant"})
    Page<Review> findByUser_IdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    long countByRestaurantId(Long restaurantId);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.restaurant.id = :restaurantId")
    double findAverageRatingByRestaurantId(@Param("restaurantId") Long restaurantId);
}
