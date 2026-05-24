package com.example.dbmatzip.domain.review.service;

import com.example.dbmatzip.domain.member.entity.User;
import com.example.dbmatzip.domain.member.repository.UserRepository;
import com.example.dbmatzip.domain.restaurant.entity.Restaurant;
import com.example.dbmatzip.domain.restaurant.exception.RestaurantNotFoundException;
import com.example.dbmatzip.domain.restaurant.repository.RestaurantRepository;
import com.example.dbmatzip.domain.review.dto.ReviewCreateRequest;
import com.example.dbmatzip.domain.review.dto.ReviewResponse;
import com.example.dbmatzip.domain.review.entity.Review;
import com.example.dbmatzip.domain.review.repository.ReviewRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse upsert(Long restaurantId, Long userId, ReviewCreateRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자 정보를 찾을 수 없습니다."));

        Review review = reviewRepository.findByRestaurantIdAndUserId(restaurantId, userId)
                .map(existing -> {
                    existing.update(request.content(), request.rating());
                    return existing;
                })
                .orElseGet(() -> Review.create(restaurant, user, request.content(), request.rating()));

        Review saved = reviewRepository.save(review);
        refreshRestaurantReviewStats(restaurant);
        return ReviewResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ReviewResponse> list(Long restaurantId, int page, int size) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException(restaurantId);
        }
        return reviewRepository.findByRestaurantId(restaurantId, PageRequest.of(page, size))
                .map(ReviewResponse::from);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ReviewResponse> listMine(Long userId, int page, int size) {
        return reviewRepository.findByUser_IdOrderByUpdatedAtDesc(userId, PageRequest.of(page, size))
                .map(r -> ReviewResponse.from(r, r.getRestaurant().getName()));
    }

    @Transactional
    public void delete(Long restaurantId, Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));
        if (!Objects.equals(review.getRestaurant().getId(), restaurantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "식당 ID와 리뷰 식당이 일치하지 않습니다.");
        }
        if (!Objects.equals(review.getUser().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 리뷰만 삭제할 수 있습니다.");
        }

        Restaurant restaurant = review.getRestaurant();
        reviewRepository.delete(review);
        refreshRestaurantReviewStats(restaurant);
    }

    private void refreshRestaurantReviewStats(Restaurant restaurant) {
        double average = reviewRepository.findAverageRatingByRestaurantId(restaurant.getId());
        int count = Math.toIntExact(reviewRepository.countByRestaurantId(restaurant.getId()));
        restaurant.setRating(count == 0 ? null : average);
        restaurant.setReviewCount(count);
    }
}
