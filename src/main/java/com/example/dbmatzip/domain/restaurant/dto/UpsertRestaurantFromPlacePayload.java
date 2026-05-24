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
        /** 장소 카테고리 문자열(선택). 없으면 "기타" 쪽 분류될 수 있습니다. */
        String category,
        String address,
        String roadAddress,
        String phone,
        @NotNull Double latitude,
        @NotNull Double longitude) {}
