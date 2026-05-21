package com.example.dbmatzip;

import com.example.dbmatzip.integration.kakao.KakaoApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(KakaoApiProperties.class)
public class DBmatzipApplication {

    public static void main(String[] args) {
        SpringApplication.run(DBmatzipApplication.class, args);
    }

}
