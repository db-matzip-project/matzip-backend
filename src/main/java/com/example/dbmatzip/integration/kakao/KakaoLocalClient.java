package com.example.dbmatzip.integration.kakao;

import com.example.dbmatzip.integration.kakao.dto.KakaoKeywordSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@Component
@RequiredArgsConstructor
public class KakaoLocalClient {

    private final RestClient kakaoLocalRestClient;

    /**
     * 키워드 장소 검색. <a href="https://developers.kakao.com/docs/latest/ko/local/dev-guide#search-by-keyword">문서</a>
     *
     * @param x 경도(중심), y 위도(중심). 둘 다 있으면 반경 검색에 사용.
     * @param radius 반경(m). 중심 좌표가 있을 때만 의미 있음. null 이면 카카오 기본.
     */
    public KakaoKeywordSearchResponse searchKeyword(
            String query, int page, int size, Double x, Double y, Integer radius) {
        return kakaoLocalRestClient
                .get()
                .uri(uriBuilder -> buildKeywordUri(uriBuilder, query, page, size, x, y, radius))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        (request, response) -> {
                            throw new KakaoApiException(
                                    "카카오 로컬 API 호출 실패: HTTP " + response.getStatusCode());
                        })
                .body(KakaoKeywordSearchResponse.class);
    }

    private static java.net.URI buildKeywordUri(
            UriBuilder b, String query, int page, int size, Double x, Double y, Integer radius) {
        UriBuilder builder = b.path("/v2/local/search/keyword.json")
                .queryParam("query", query)
                .queryParam("page", page)
                .queryParam("size", size);
        if (x != null && y != null) {
            builder = builder.queryParam("x", x).queryParam("y", y);
            if (radius != null) {
                builder = builder.queryParam("radius", radius);
            }
        }
        return builder.build();
    }
}
