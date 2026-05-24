package com.example.dbmatzip.domain.member.controller;

import com.example.dbmatzip.domain.restaurant.dto.PageResponse;
import com.example.dbmatzip.domain.review.dto.ReviewResponse;
import com.example.dbmatzip.domain.review.service.ReviewService;
import com.example.dbmatzip.domain.member.dto.UserProfileResponse;
import com.example.dbmatzip.domain.member.dto.UserProfileUpdateRequest;
import com.example.dbmatzip.domain.member.service.MemberProfileService;
import com.example.dbmatzip.global.security.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class MemberController {

    private final MemberProfileService memberProfileService;
    private final ReviewService reviewService;

    @GetMapping("/me")
    public UserProfileResponse me(@AuthenticationPrincipal MemberPrincipal principal) {
        return memberProfileService.getProfile(principal.getId());
    }

    @GetMapping("/me/reviews")
    @Operation(summary = "내 리뷰 목록", description = "최근 수정 순. restaurantName 포함.")
    public PageResponse<ReviewResponse> myReviews(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(reviewService.listMine(principal.getId(), page, size));
    }

    @PatchMapping("/me")
    public UserProfileResponse patchMe(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        return memberProfileService.updateProfile(principal.getId(), request);
    }
}
