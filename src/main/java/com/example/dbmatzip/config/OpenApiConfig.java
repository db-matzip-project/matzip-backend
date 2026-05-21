package com.example.dbmatzip.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI에서 JWT를 넣어 보호된 API를 호출할 수 있도록 Bearer 스키마를 등록합니다.
 * 브라우저: <code>/swagger-ui.html</code>
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("DBmatzip API")
                                .description("로그인 후 응답의 accessToken을 Authorize 에 넣으면 됩니다. (Bearer 접두어 없이 토큰만)")
                                .version("v1"))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        BEARER_AUTH,
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")));
    }
}
