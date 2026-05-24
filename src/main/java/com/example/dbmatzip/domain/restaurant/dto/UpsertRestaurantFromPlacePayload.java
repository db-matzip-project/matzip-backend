package com.example.dbmatzip.domain.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 프론트가 카카오 장소 검색 결과(등)에서 넘긴 정보로 레스토랑 행을 upsert 할 때 사용.
 * PostGIS 검색 및 GIST 표현식 인덱스는 기존과 같이 latitude·longitude 숫자 컬럼을 사용합니다.
 */
public record UpsertRestaurantFromPlacePayload(
        /** 카카오 Local 문서의 id 와 동일 (JSON 필드 이름 id 도 허용) */
        @JsonAlias("id") @NotBlank String apiId,
        @NotBlank String name,
        /** 카카오 {@code category_name} 전체 문자열 (예: {@code 음식점 > 한식 > 국밥}) */
        @JsonAlias({"category_name"}) String category,
        /** 카카오 대분류 코드 (예: {@code FD6} 음식점, {@code CE7} 카페). 있으면 분류 정확도가 올라갑니다. */
        @JsonAlias("category_group_code") String categoryGroupCode,
        /** 카카오 {@code category_group_name} (선택). */
        @JsonAlias("category_group_name") String categoryGroupName,
        String address,
        String roadAddress,
        String phone,
        @NotNull Double latitude,
        @NotNull Double longitude) {}
