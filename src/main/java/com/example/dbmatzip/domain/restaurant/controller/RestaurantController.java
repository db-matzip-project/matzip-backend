package com.example.dbmatzip.domain.restaurant.controller;

import com.example.dbmatzip.domain.restaurant.dto.KakaoImportRequest;
import com.example.dbmatzip.domain.restaurant.dto.KakaoImportResponse;
import com.example.dbmatzip.domain.restaurant.dto.PageResponse;
import com.example.dbmatzip.domain.restaurant.dto.RestaurantResponse;
import com.example.dbmatzip.domain.restaurant.service.KakaoRestaurantImportService;
import com.example.dbmatzip.domain.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
     * tasteSimilar=true 이면 Authorization: Bearer JWT 필요. 입맛 비슷한 사용자들이 최근 일정에 많이 담은 식당 ID 와 bounds 교집합.
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
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "false") boolean tasteSimilar,
            Authentication authentication) {
        return restaurantService.search(
                category, minRating, minLat, minLng, maxLat, maxLng, page, size, sort, tasteSimilar, authentication);
    }

    @GetMapping("/{id}")
    public RestaurantResponse detail(@PathVariable Long id) {
        return restaurantService.getById(id);
    }

    @PostMapping("/import/kakao")
    public KakaoImportResponse importFromKakao(@Valid @RequestBody KakaoImportRequest request) {
        return kakaoRestaurantImportService.importFromKeyword(request);
    }
}
