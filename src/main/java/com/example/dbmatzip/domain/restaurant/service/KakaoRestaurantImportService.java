package com.example.dbmatzip.domain.restaurant.service;

import com.example.dbmatzip.domain.restaurant.dto.KakaoImportRequest;
import com.example.dbmatzip.domain.restaurant.dto.KakaoImportResponse;
import com.example.dbmatzip.integration.kakao.KakaoApiProperties;
import com.example.dbmatzip.integration.kakao.KakaoLocalClient;
import com.example.dbmatzip.integration.kakao.dto.KakaoKeywordSearchResponse;
import com.example.dbmatzip.integration.kakao.dto.KakaoPlaceDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KakaoRestaurantImportService {

    private final KakaoApiProperties kakaoApiProperties;
    private final KakaoLocalClient kakaoLocalClient;
    private final RestaurantUpsertService restaurantUpsertService;

    @Transactional
    public KakaoImportResponse importFromKeyword(KakaoImportRequest request) {
        ensureApiKey();

        int page = request.page() == null ? 1 : request.page();
        int size = request.size() == null ? 15 : request.size();

        KakaoKeywordSearchResponse response =
                kakaoLocalClient.searchKeyword(request.query(), page, size, request.x(), request.y(), request.radius());

        int saved = 0;
        for (KakaoPlaceDocument doc : response.documents()) {
            if (restaurantUpsertService.upsertFromKakaoKeywordDocument(doc).isPresent()) {
                saved++;
            }
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
}
