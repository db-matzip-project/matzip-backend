package com.example.dbmatzip.domain.restaurant.config;

import com.example.dbmatzip.domain.restaurant.entity.Restaurant;
import com.example.dbmatzip.domain.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 로컬 데모용 샘플 식당. 운영에서는 비활성(profile 에 dev 미포함).
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class RestaurantSampleDataLoader implements ApplicationRunner {

    private final RestaurantRepository restaurantRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (restaurantRepository.count() > 0) {
            return;
        }
        restaurantRepository.save(sample("kakao-demo-1", "샘플 한식당", "KOREAN", 37.5665, 126.9780));
        restaurantRepository.save(sample("kakao-demo-2", "샘플 카페", "CAFE", 37.5700, 126.9820));
        restaurantRepository.save(sample("kakao-demo-3", "샘플 일식", "JAPANESE", 37.5630, 126.9750));
    }

    private static Restaurant sample(String apiId, String name, String category, double lat, double lon) {
        Restaurant r = new Restaurant();
        r.setApiId(apiId);
        r.setName(name);
        r.setCategory(category);
        r.setAddress("데모 지번 주소");
        r.setRoadAddress("데모 도로명 주소");
        r.setPhone("02-0000-0000");
        r.setDescription("dev 샘플 데이터");
        r.setLatitude(lat);
        r.setLongitude(lon);
        r.setRating(4.2);
        r.setReviewCount(12);
        r.setScheduleAddCount(0);
        return r;
    }
}
