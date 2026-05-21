package com.example.dbmatzip.config;

import com.example.dbmatzip.domain.preference.entity.Preference;
import com.example.dbmatzip.domain.preference.repository.PreferenceRepository;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PreferenceSeedConfig {

    @Bean
    ApplicationRunner preferenceSeedRunner(PreferenceRepository preferenceRepository) {
        return args -> {
            if (preferenceRepository.count() > 0) {
                return;
            }
            List<Preference> seeds =
                    List.of(
                            Preference.builder().code("SPICY_LOW").displayName("약간 매운 편").build(),
                            Preference.builder().code("SPICY_MED").displayName("보통 매움").build(),
                            Preference.builder().code("SPICY_HIGH").displayName("아주 매운 편").build(),
                            Preference.builder().code("SWEET").displayName("단 맛 선호").build(),
                            Preference.builder().code("SALTY").displayName("짠 맛 선호").build(),
                            Preference.builder().code("LIGHT").displayName("담백한 맛").build(),
                            Preference.builder().code("OILY").displayName("기름진 음식 OK").build(),
                            Preference.builder().code("RAW_OK").displayName("회·사시미 좋아함").build(),
                            Preference.builder().code("NO_PORK").displayName("돼지고기 비선호").build(),
                            Preference.builder().code("VEGETARIAN").displayName("채식 위주").build());
            preferenceRepository.saveAll(seeds);
        };
    }
}
