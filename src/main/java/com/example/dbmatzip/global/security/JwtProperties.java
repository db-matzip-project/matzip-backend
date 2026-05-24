package com.example.dbmatzip.global.security;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** HS256 최소 256비트 권장 — 운영/개발 모두 환경변수로 반드시 설정 */
    private String secret;

    /** 만료(ms). 기본 24시간 */
    private long expirationMs = 86_400_000L;

    @PostConstruct
    void validate() {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("jwt.secret 이 비어 있습니다. JWT_SECRET 환경변수를 설정하세요.");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException("jwt.secret 은 최소 32자 이상이어야 합니다.");
        }
    }
}
