package com.example.dbmatzip.domain.restaurant.service;

import com.example.dbmatzip.domain.restaurant.dto.KakaoImportRequest;
import com.example.dbmatzip.domain.restaurant.dto.KakaoImportResponse;
import com.example.dbmatzip.domain.restaurant.entity.Restaurant;
import com.example.dbmatzip.domain.restaurant.repository.RestaurantRepository;
import com.example.dbmatzip.integration.kakao.KakaoApiProperties;
import com.example.dbmatzip.integration.kakao.KakaoLocalClient;
import com.example.dbmatzip.integration.kakao.dto.KakaoKeywordSearchResponse;
import com.example.dbmatzip.integration.kakao.dto.KakaoPlaceDocument;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KakaoRestaurantImportService {

    private final KakaoApiProperties kakaoApiProperties;
    private final KakaoLocalClient kakaoLocalClient;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public KakaoImportResponse importFromKeyword(KakaoImportRequest request) {
        ensureApiKey();

        int page = request.page() == null ? 1 : request.page();
        int size = request.size() == null ? 15 : request.size();

        KakaoKeywordSearchResponse response =
                kakaoLocalClient.searchKeyword(request.query(), page, size, request.x(), request.y(), request.radius());

        int saved = 0;
        for (KakaoPlaceDocument doc : response.documents()) {
            if (doc.id() == null || doc.id().isBlank() || doc.x() == null || doc.y() == null) {
                continue;
            }
            Optional<Restaurant> existing = restaurantRepository.findByApiId(doc.id());
            Restaurant entity = existing.orElseGet(Restaurant::new);
            try {
                applyDocument(doc, entity);
            } catch (NumberFormatException ex) {
                continue;
            }
            restaurantRepository.save(entity);
            saved++;
        }
        return new KakaoImportResponse(response.documents().size(), saved);
    }

    private void ensureApiKey() {
        String key = kakaoApiProperties.restApiKey();
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "kakao.rest-api-key 가 비어 있습니다. 환경변수 KAKAO_REST_API_KEY 또는 application.yml 을 설정하세요.");
        }
    }

    private static void applyDocument(KakaoPlaceDocument doc, Restaurant r) {
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
        // 카카오 키워드 검색 응답에는 평점·리뷰 수가 없으면 null 유지 (별도 API·통계로 보강)
    }

    private static String truncate(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }

    private static String normalizeCategory(String categoryName, String categoryGroupName, String categoryGroupCode) {
        String source = ((categoryName == null ? "" : categoryName) + " "
                        + (categoryGroupName == null ? "" : categoryGroupName) + " "
                        + (categoryGroupCode == null ? "" : categoryGroupCode))
                .toLowerCase();

        if (containsAny(source, "디저트", "카페", "베이커리", "커피", "tea", "fd6")) {
            // fd6 alone is broad(음식점) but most imported rows include categoryName too.
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
