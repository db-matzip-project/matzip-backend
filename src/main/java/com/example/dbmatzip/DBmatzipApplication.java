package com.example.dbmatzip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DBmatzipApplication {

    public static void main(String[] args) {
        SpringApplication.run(DBmatzipApplication.class, args);
    }

}
