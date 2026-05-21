package com.example.dbmatzip.domain.restaurant.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KakaoImportRequest(
        @NotBlank @Size(max = 200) String query,
        /** 경도 (중심). 위도와 함께 넣으면 반경 검색 */
        Double x,
        /** 위도 (중심) */
        Double y,
        @Min(1) @Max(20000) Integer radius,
        @Min(1) @Max(45) Integer page,
        @Min(1) @Max(15) Integer size) {}
