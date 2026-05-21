package com.example.dbmatzip.integration.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoKeywordSearchResponse(List<KakaoPlaceDocument> documents) {
    public KakaoKeywordSearchResponse {
        if (documents == null) {
            documents = List.of();
        }
    }
}
