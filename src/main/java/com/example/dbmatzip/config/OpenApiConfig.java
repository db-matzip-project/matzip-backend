package com.example.dbmatzip.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement; // 💡 추가됨
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI openAPI() {
        // 모든 API에 기본적으로 JWT 인증을 적용하도록 요구사항 정의
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(BEARER_AUTH); // 💡 추가됨

        return new OpenAPI()
                .info(
                        new Info()
                                .title("DBmatzip API")
                                .description(
                                        "로그인 후 응답의 accessToken을 Authorize 에 넣으면 됩니다. Swagger UI는 Bearer 접두어를 자동으로 처리합니다.")
                                .version("v1"))
                .addSecurityItem(securityRequirement) // 💡 추가됨: 모든 엔드포인트에 자물쇠 매핑
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