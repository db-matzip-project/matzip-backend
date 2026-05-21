package com.example.dbmatzip;

import com.example.dbmatzip.integration.kakao.KakaoApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties(KakaoApiProperties.class)
public class DBmatzipApplication {

    public static void main(String[] args) {
        SpringApplication.run(DBmatzipApplication.class, args);
    }

}
