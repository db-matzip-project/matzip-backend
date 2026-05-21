package com.example.dbmatzip.integration.kakao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class KakaoLocalApiConfig {

    @Bean
    public RestClient kakaoLocalRestClient(KakaoApiProperties properties) {
        String key = properties.restApiKey() == null ? "" : properties.restApiKey().trim();
        return RestClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + key)
                .build();
    }
}
