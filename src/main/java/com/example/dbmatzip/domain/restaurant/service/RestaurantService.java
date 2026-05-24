package com.example.dbmatzip.domain.restaurant.service;

import com.example.dbmatzip.domain.analytics.repository.ScheduleAnalyticsRepository;
import com.example.dbmatzip.domain.restaurant.dto.PageResponse;
import com.example.dbmatzip.domain.restaurant.dto.RestaurantResponse;
import com.example.dbmatzip.domain.restaurant.entity.Restaurant;
import com.example.dbmatzip.domain.restaurant.exception.RestaurantNotFoundException;
import com.example.dbmatzip.domain.restaurant.model.RestaurantCategory;
import com.example.dbmatzip.domain.restaurant.repository.RestaurantRepository;
import com.example.dbmatzip.global.security.MemberPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final ScheduleAnalyticsRepository scheduleAnalyticsRepository;

    private static final int MAX_RECOMMENDED_IDS = 400;

    public PageResponse<RestaurantResponse> search(
            String category,
            Double minRating,
            Double minLat,
            Double minLng,
            Double maxLat,
            Double maxLng,
            int page,
            int size,
            String sortBy,
            String sort,
            boolean tasteSimilar,
            Authentication authentication) {

        validateBoundingBox(minLat, minLng, maxLat, maxLng);
        String categoryArg = validatedCategoryOrNull(category);
        String normalizedSort = normalizeSort(sortBy, sort);
        boolean sortByDistance = isDistanceSort(normalizedSort);
        Pageable pageable = PageRequest.of(page, size);

        if (tasteSimilar) {
            Long userId = requireUserId(authentication);
            List<Long> ids = scheduleAnalyticsRepository.findRecommendedRestaurantIds(userId, MAX_RECOMMENDED_IDS);
            if (ids.isEmpty()) {
                // 유사 사용자 데이터가 부족한 초기 상태에서는 빈 화면 대신 일반 검색 결과로 폴백합니다.
                return searchWithoutTasteSimilar(
                        category,
                        minRating,
                        minLat,
                        minLng,
                        maxLat,
                        maxLng,
                        pageable,
                        sortByDistance,
                        normalizedSort);
            }
            Page<Restaurant> result = sortByDistance
                    ? restaurantRepository.searchAmongIdsOrderByDistance(
                            ids,
                            categoryArg,
                            minRating,
                            minLat,
                            minLng,
                            maxLat,
                            maxLng,
                            resolveCenterLat(minLat, maxLat),
                            resolveCenterLng(minLng, maxLng),
                            pageable)
                    : restaurantRepository.searchAmongIds(
                            ids,
                            categoryArg,
                            minRating,
                            minLat,
                            minLng,
                            maxLat,
                            maxLng,
                            normalizedSort,
                            pageable);
            return PageResponse.of(result.map(RestaurantResponse::from));
        }

        Page<Restaurant> result = sortByDistance
                ? restaurantRepository.searchOrderByDistance(
                        categoryArg,
                        minRating,
                        minLat,
                        minLng,
                        maxLat,
                        maxLng,
                        resolveCenterLat(minLat, maxLat),
                        resolveCenterLng(minLng, maxLng),
                        pageable)
                : restaurantRepository.search(categoryArg, minRating, minLat, minLng, maxLat, maxLng, normalizedSort, pageable);
        return PageResponse.of(result.map(RestaurantResponse::from));
    }

    private PageResponse<RestaurantResponse> searchWithoutTasteSimilar(
            String category,
            Double minRating,
            Double minLat,
            Double minLng,
            Double maxLat,
            Double maxLng,
            Pageable pageable,
            boolean sortByDistance,
            String normalizedSort) {
        Page<Restaurant> fallback = sortByDistance
                ? restaurantRepository.searchOrderByDistance(
                        category,
                        minRating,
                        minLat,
                        minLng,
                        maxLat,
                        maxLng,
                        resolveCenterLat(minLat, maxLat),
                        resolveCenterLng(minLng, maxLng),
                        pageable)
                : restaurantRepository.search(
                        category, minRating, minLat, minLng, maxLat, maxLng, normalizedSort, pageable);
        return PageResponse.of(fallback.map(RestaurantResponse::from));
    }

    private static Long requireUserId(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "입맛 추천 검색은 로그인이 필요합니다.");
        }
        Object p = authentication.getPrincipal();
        if (p instanceof MemberPrincipal mp) {
            return mp.getId();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "입맛 추천 검색은 로그인이 필요합니다.");
    }

    public RestaurantResponse getById(Long id) {
        Restaurant restaurant =
                restaurantRepository.findById(id).orElseThrow(() -> new RestaurantNotFoundException(id));
        return RestaurantResponse.from(restaurant);
    }

    private static String validatedCategoryOrNull(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        String trimmed = category.trim();
        if (!RestaurantCategory.ALLOWED_LABELS.contains(trimmed)) {
            throw new IllegalArgumentException(
                    "category 는 "
                            + String.join(", ", RestaurantCategory.ALLOWED_SEARCH_VALUES)
                            + " 중 하나만 허용됩니다.");
        }
        return trimmed;
    }

    private static void validateBoundingBox(Double minLat, Double minLng, Double maxLat, Double maxLng) {
        long present = countNonNull(minLat, minLng, maxLat, maxLng);
        if (present != 0 && present != 4) {
            throw new IllegalArgumentException(
                    "지도 검색을 쓰려면 minLat, minLng, maxLat, maxLng 를 모두 보내거나 모두 생략하세요.");
        }
        if (present == 4 && (minLat >= maxLat || minLng >= maxLng)) {
            throw new IllegalArgumentException("유효한 사각형이 아닙니다. min < max 인지 확인하세요.");
        }
    }

    private static long countNonNull(Object... values) {
        long n = 0;
        for (Object v : values) {
            if (v != null) {
                n++;
            }
        }
        return n;
    }

    private static String normalizeSort(String sortBy, String legacySort) {
        String candidate = (sortBy != null && !sortBy.isBlank()) ? sortBy : legacySort;
        if (candidate == null || candidate.isBlank()) {
            return "rating_desc";
        }
        String normalized = candidate.trim().toLowerCase();
        return switch (normalized) {
            case "rating", "rating_desc", "rating,desc" -> "rating_desc";
            case "rating_asc", "rating,asc" -> "rating_asc";
            case "reviews", "review", "reviewcount,desc", "review_count_desc", "reviewcount_desc" ->
                    "review_count_desc";
            case "reviewcount,asc", "review_count_asc", "reviewcount_asc" -> "review_count_asc";
            case "distance", "distance,asc", "distance_asc" -> "distance_asc";
            default -> "rating_desc";
        };
    }

    private static boolean isDistanceSort(String sort) {
        return "distance_asc".equals(sort);
    }

    private static double resolveCenterLat(Double minLat, Double maxLat) {
        if (minLat == null || maxLat == null) {
            return 37.5665;
        }
        return (minLat + maxLat) / 2.0;
    }

    private static double resolveCenterLng(Double minLng, Double maxLng) {
        if (minLng == null || maxLng == null) {
            return 126.9780;
        }
        return (minLng + maxLng) / 2.0;
    }
}
