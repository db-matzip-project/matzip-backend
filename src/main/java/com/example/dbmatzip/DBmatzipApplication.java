package com.example.dbmatzip;

import com.example.dbmatzip.global.security.JwtProperties;
import com.example.dbmatzip.integration.kakao.KakaoApiProperties;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties({KakaoApiProperties.class, JwtProperties.class})
public class DBmatzipApplication {

    /**
     * Spring 은 프로젝트 루트 .env 를 읽지 않으므로, 기동 최초에만 JVM 프로퍼티로 적재합니다.
     * OS 환경변수가 있으면 덮어쓰지 않습니다. 같은 키는 나중에 읽은 .env 파일이 우선입니다.
     */
    static {
        loadDotEnvIntoSystemPropertiesIfMissingInEnvironment();
    }

    public static void main(String[] args) {
        SpringApplication.run(DBmatzipApplication.class, args);
    }

    private static void loadDotEnvIntoSystemPropertiesIfMissingInEnvironment() {
        Map<String, String> merged = new LinkedHashMap<>();
        Path[] roots = new Path[] {Path.of(".."), Path.of(".")}; // 나중(. )이 이김 — 백엔드 루트 .env 우선
        for (Path root : roots) {
            Path envPath = root.resolve(".env").toAbsolutePath().normalize();
            if (!Files.isRegularFile(envPath)) {
                continue;
            }
            try {
                Dotenv dotenv = Dotenv.configure()
                        .directory(envPath.getParent().toString())
                        .filename(envPath.getFileName().toString())
                        .ignoreIfMalformed()
                        .load();
                for (DotenvEntry e : dotenv.entries()) {
                    if (e.getValue() != null) {
                        merged.put(e.getKey(), e.getValue());
                    }
                }
            } catch (Exception ignored) {
                /* 선택적 로컬 파일 */
            }
        }
        for (Map.Entry<String, String> entry : merged.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String env = System.getenv(key);
            if (env != null && !env.isBlank()) {
                continue;
            }
            if (System.getProperty(key) == null || System.getProperty(key).isEmpty()) {
                System.setProperty(key, value);
            }
        }
    }
}
