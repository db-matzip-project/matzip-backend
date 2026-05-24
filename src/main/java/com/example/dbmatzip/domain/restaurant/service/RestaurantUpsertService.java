package com.example.dbmatzip.domain.restaurant.service;

import com.example.dbmatzip.domain.restaurant.dto.UpsertRestaurantFromPlacePayload;
import com.example.dbmatzip.domain.restaurant.entity.Restaurant;
import com.example.dbmatzip.domain.restaurant.repository.RestaurantRepository;
import com.example.dbmatzip.integration.kakao.dto.KakaoPlaceDocument;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 외부(카카오 등) 장소 식별자(api_id) 기준 레스토랑 upsert. 좌표는 latitude·longitude 에 저장하여
 * 기존 PostGIS 표현식 GIST(ST_MakePoint) 검색 경로와 동일하게 유지합니다.
 */
@Service
@RequiredArgsConstructor
public class RestaurantUpsertService {

    private final RestaurantRepository restaurantRepository;

    @Transactional
    public Optional<Restaurant> upsertFromKakaoKeywordDocument(KakaoPlaceDocument doc) {
        if (doc.id() == null || doc.id().isBlank() || doc.x() == null || doc.y() == null) {
            return Optional.empty();
        }
        Restaurant entity = restaurantRepository.findByApiId(doc.id()).orElseGet(Restaurant::new);
        try {
            applyKakaoDocument(doc, entity);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
        return Optional.of(restaurantRepository.save(entity));
    }

    @Transactional
    public Restaurant upsertFromExternalPlace(UpsertRestaurantFromPlacePayload payload) {
        if (!Double.isFinite(payload.latitude()) || !Double.isFinite(payload.longitude())) {
            throw new IllegalArgumentException("latitude·longitude 에 유한한 숫자를 입력해 주세요.");
        }

        Restaurant entity = restaurantRepository.findByApiId(payload.apiId()).orElseGet(Restaurant::new);
        applyExternalPayload(payload, entity);
        return restaurantRepository.save(entity);
    }

    private static void applyKakaoDocument(KakaoPlaceDocument doc, Restaurant r) {
        r.setApiId(doc.id());
        r.setName(truncate(doc.placeName(), 200));
        r.setCategory(normalizeCategory(doc.categoryName(), doc.categoryGroupName(), doc.categoryGroupCode()));
        r.setAddress(truncate(doc.addressName(), 500));
        r.setRoadAddress(truncate(doc.roadAddressName(), 500));
        r.setPhone(truncate(doc.phone(), 40));
        r.setDescription(truncate(doc.categoryName(), 2000));

        double lat = Double.parseDouble(doc.y());
        double lon = Double.parseDouble(doc.x());
        r.setLatitude(lat);
        r.setLongitude(lon);
    }

    private static void applyExternalPayload(UpsertRestaurantFromPlacePayload p, Restaurant r) {
        r.setApiId(p.apiId());
        r.setName(truncate(p.name(), 200));
        r.setCategory(normalizeCategory(p.category(), null, null));
        r.setAddress(truncate(p.address(), 500));
        r.setRoadAddress(truncate(p.roadAddress(), 500));
        r.setPhone(truncate(p.phone(), 40));
        r.setDescription(truncate(p.category(), 2000));
        r.setLatitude(p.latitude());
        r.setLongitude(p.longitude());
    }

    private static String truncate(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }

    /** Kakao 카테고리 문자열 또는 프론트가 넘긴 단일 category 문자열을 내부 코드로 근사 매핑 */
    static String normalizeCategory(String categoryName, String categoryGroupName, String categoryGroupCode) {
        String source =
                ((categoryName == null ? "" : categoryName) + " "
                                + (categoryGroupName == null ? "" : categoryGroupName) + " "
                                + (categoryGroupCode == null ? "" : categoryGroupCode))
                        .toLowerCase();

        if (containsAny(source, "디저트", "카페", "베이커리", "커피", "tea", "fd6")) {
            if (containsAny(source, "디저트", "카페", "베이커리", "커피", "tea")) {
                return "디저트";
            }
        }
        if (containsAny(source, "채식", "비건", "샐러드")) {
            return "채식";
        }
        if (containsAny(source, "일식", "일본", "초밥", "스시", "라멘", "돈까스", "이자카야")) {
            return "일식";
        }
        if (containsAny(source, "중식", "중국", "짜장", "마라", "딤섬")) {
            return "중식";
        }
        if (containsAny(source, "양식", "이탈", "프랑", "스테이크", "파스타", "피자", "브런치")) {
            return "양식";
        }
        if (containsAny(source, "한식", "국밥", "찌개", "분식", "보쌈", "족발", "korean")) {
            return "한식";
        }
        return "기타";
    }

    private static boolean containsAny(String source, String... keywords) {
        for (String keyword : keywords) {
            if (source.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
