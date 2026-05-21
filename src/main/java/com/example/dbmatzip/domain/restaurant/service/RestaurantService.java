package com.example.dbmatzip.domain.restaurant.service;

import com.example.dbmatzip.domain.analytics.repository.ScheduleAnalyticsRepository;
import com.example.dbmatzip.domain.restaurant.dto.PageResponse;
import com.example.dbmatzip.domain.restaurant.dto.RestaurantResponse;
import com.example.dbmatzip.domain.restaurant.entity.Restaurant;
import com.example.dbmatzip.domain.restaurant.exception.RestaurantNotFoundException;
import com.example.dbmatzip.domain.restaurant.repository.RestaurantRepository;
import com.example.dbmatzip.global.security.MemberPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            String sort,
            boolean tasteSimilar,
            Authentication authentication) {

        validateBoundingBox(minLat, minLng, maxLat, maxLng);
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));

        if (tasteSimilar) {
            Long userId = requireUserId(authentication);
            List<Long> ids = scheduleAnalyticsRepository.findRecommendedRestaurantIds(userId, MAX_RECOMMENDED_IDS);
            if (ids.isEmpty()) {
                return PageResponse.of(Page.empty(pageable));
            }
            Page<Restaurant> result =
                    restaurantRepository.searchAmongIds(ids, category, minRating, minLat, minLng, maxLat, maxLng, pageable);
            return PageResponse.of(result.map(RestaurantResponse::from));
        }

        Page<Restaurant> result =
                restaurantRepository.search(category, minRating, minLat, minLng, maxLat, maxLng, pageable);
        return PageResponse.of(result.map(RestaurantResponse::from));
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

    private static Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "rating");
        }
        return switch (sort.trim().toLowerCase()) {
            case "rating_asc" -> Sort.by(Sort.Direction.ASC, "rating");
            case "rating_desc" -> Sort.by(Sort.Direction.DESC, "rating");
            case "name_asc" -> Sort.by(Sort.Direction.ASC, "name");
            default -> Sort.by(Sort.Direction.DESC, "rating");
        };
    }
}
