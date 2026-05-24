package com.example.dbmatzip.domain.review.controller;

import com.example.dbmatzip.domain.restaurant.dto.PageResponse;
import com.example.dbmatzip.domain.review.dto.ReviewCreateRequest;
import com.example.dbmatzip.domain.review.dto.ReviewResponse;
import com.example.dbmatzip.domain.review.service.ReviewService;
import com.example.dbmatzip.global.security.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "리뷰 작성/수정", description = "사용자당 식당 1개 리뷰 정책으로 같은 식당에 재작성하면 수정됩니다.")
    public ReviewResponse upsert(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody ReviewCreateRequest request) {
        return reviewService.upsert(restaurantId, principal.getId(), request);
    }

    @GetMapping
    @Operation(summary = "식당 리뷰 목록")
    public PageResponse<ReviewResponse> list(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(reviewService.list(restaurantId, page, size));
    }

    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "리뷰 삭제", description = "본인 리뷰만 삭제할 수 있습니다.")
    public void delete(
            @PathVariable Long restaurantId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal MemberPrincipal principal) {
        reviewService.delete(restaurantId, reviewId, principal.getId());
    }
}
