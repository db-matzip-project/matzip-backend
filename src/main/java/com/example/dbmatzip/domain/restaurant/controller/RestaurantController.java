package com.example.dbmatzip.domain.restaurant.controller;

import com.example.dbmatzip.domain.restaurant.dto.KakaoImportRequest;
import com.example.dbmatzip.domain.restaurant.dto.KakaoImportResponse;
import com.example.dbmatzip.domain.restaurant.dto.PageResponse;
import com.example.dbmatzip.domain.restaurant.dto.RestaurantResponse;
import com.example.dbmatzip.domain.restaurant.service.KakaoRestaurantImportService;
import com.example.dbmatzip.domain.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final KakaoRestaurantImportService kakaoRestaurantImportService;

    /**
     * 목록·검색. 지도 bounds는 minLat, minLng, maxLat, maxLng 네 값 모두 전달 — PostGIS
     * {@code ST_Contains(ST_MakeEnvelope(...), ST_MakePoint(lon,lat))}.
     *
     * @param sort rating_desc(기본) | rating_asc | name_asc
     */
    @GetMapping
    public PageResponse<RestaurantResponse> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double minLat,
            @RequestParam(required = false) Double minLng,
            @RequestParam(required = false) Double maxLat,
            @RequestParam(required = false) Double maxLng,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        return restaurantService.search(category, minRating, minLat, minLng, maxLat, maxLng, page, size, sort);
    }

    @GetMapping("/{id}")
    public RestaurantResponse detail(@PathVariable Long id) {
        return restaurantService.getById(id);
    }

    /**
     * 카카오 로컬 API 키워드 검색 결과를 DB에 저장·갱신합니다. 동일 API 장소 id(api_id)는 upsert.
     */
    @PostMapping("/import/kakao")
    public KakaoImportResponse importFromKakao(@Valid @RequestBody KakaoImportRequest request) {
        return kakaoRestaurantImportService.importFromKeyword(request);
    }
}
