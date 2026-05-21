package com.example.dbmatzip.global.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** HS256 최소 256비트 권장 — 배포 시 반드시 환경변수로 덮어쓰기 */
    private String secret = "dev-dev-dev-dev-dev-dev-dev-dev-dev-dev-dev-dev-dev-change-me-in-production";

    /** 만료(ms). 기본 24시간 */
    private long expirationMs = 86_400_000L;
}
