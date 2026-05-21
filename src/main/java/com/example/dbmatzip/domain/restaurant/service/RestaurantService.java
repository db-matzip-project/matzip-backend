package com.example.dbmatzip.domain.restaurant.service;

import com.example.dbmatzip.domain.restaurant.dto.PageResponse;
import com.example.dbmatzip.domain.restaurant.dto.RestaurantResponse;
import com.example.dbmatzip.domain.restaurant.entity.Restaurant;
import com.example.dbmatzip.domain.restaurant.exception.RestaurantNotFoundException;
import com.example.dbmatzip.domain.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public PageResponse<RestaurantResponse> search(
            String category,
            Double minRating,
            Double minLat,
            Double minLng,
            Double maxLat,
            Double maxLng,
            int page,
            int size,
            String sort) {

        validateBoundingBox(minLat, minLng, maxLat, maxLng);

        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        Page<Restaurant> result =
                restaurantRepository.search(category, minRating, minLat, minLng, maxLat, maxLng, pageable);
        return PageResponse.of(result.map(RestaurantResponse::from));
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
