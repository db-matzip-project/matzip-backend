package com.example.dbmatzip.integration.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao")
public record KakaoApiProperties(String restApiKey) {}
