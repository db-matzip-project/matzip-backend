package com.example.dbmatzip.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewCreateRequest(
        @Schema(description = "리뷰 내용", example = "맛이 깔끔하고 서비스가 좋아요.")
        @NotBlank @Size(max = 1000) String content,
        @Schema(description = "평점(1~5)", example = "5")
        @NotNull @Min(1) @Max(5) Integer rating) {}
