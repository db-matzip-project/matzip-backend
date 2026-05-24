package com.example.dbmatzip.domain.restaurant.service;

import com.example.dbmatzip.domain.restaurant.dto.UpsertRestaurantFromPlacePayload;
import com.example.dbmatzip.domain.restaurant.entity.Restaurant;
import com.example.dbmatzip.domain.restaurant.model.RestaurantCategory;
import com.example.dbmatzip.domain.restaurant.repository.RestaurantRepository;
import com.example.dbmatzip.integration.kakao.dto.KakaoPlaceDocument;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 외부(카카오 등) 장소 식별자(api_id) 기준 레스토랑 upsert. 좌표는 latitude·longitude 에 저장하여
 * 기존 PostGIS 표현식 GIST(ST_MakePoint) 검색 경로와 동일하게 유지합니다.
 */
@Service
@RequiredArgsConstructor
public class RestaurantUpsertService {

    /** {@code category_name} 에 그룹 코드만 단독으로 들어오는 레거시/오입력 호환 ({@code CE7}, {@code FD6} … ). */
    private static final Pattern KAKAO_GROUP_CODE_ONLY =
            Pattern.compile("^[A-Z]{1,4}\\d$", Pattern.CASE_INSENSITIVE);

    private final RestaurantRepository restaurantRepository;

    @Transactional
    public Optional<Restaurant> upsertFromKakaoKeywordDocument(KakaoPlaceDocument doc) {
        if (doc.id() == null || doc.id().isBlank() || doc.x() == null || doc.y() == null) {
            return Optional.empty();
        }
        Restaurant entity = restaurantRepository.findByApiId(doc.id()).orElseGet(Restaurant::new);
        try {
            applyKakaoDocument(doc, entity);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
        return Optional.of(restaurantRepository.save(entity));
    }

    @Transactional
    public Restaurant upsertFromExternalPlace(UpsertRestaurantFromPlacePayload payload) {
        if (!Double.isFinite(payload.latitude()) || !Double.isFinite(payload.longitude())) {
            throw new IllegalArgumentException("latitude·longitude 에 유한한 숫자를 입력해 주세요.");
        }

        Restaurant entity = restaurantRepository.findByApiId(payload.apiId()).orElseGet(Restaurant::new);
        applyExternalPayload(payload, entity);
        return restaurantRepository.save(entity);
    }

    private static void applyKakaoDocument(KakaoPlaceDocument doc, Restaurant r) {
        r.setApiId(doc.id());
        r.setName(truncate(doc.placeName(), 200));
        r.setCategory(
                normalizeCategory(
                        doc.categoryName(), doc.categoryGroupName(), doc.categoryGroupCode(), doc.placeName()));
        r.setAddress(truncate(doc.addressName(), 500));
        r.setRoadAddress(truncate(doc.roadAddressName(), 500));
        r.setPhone(truncate(doc.phone(), 40));
        r.setDescription(truncate(doc.categoryName(), 2000));

        double lat = Double.parseDouble(doc.y());
        double lon = Double.parseDouble(doc.x());
        r.setLatitude(lat);
        r.setLongitude(lon);
    }

    private static void applyExternalPayload(UpsertRestaurantFromPlacePayload p, Restaurant r) {
        r.setApiId(p.apiId());
        r.setName(truncate(p.name(), 200));
        r.setCategory(normalizeCategory(p.category(), p.categoryGroupName(), p.categoryGroupCode(), p.name()));

        String displayCategory = concatNonBlank(" > ", p.categoryGroupName(), p.category());
        r.setDescription(truncate(StringUtils.hasText(displayCategory) ? displayCategory : p.category(), 2000));

        r.setAddress(truncate(p.address(), 500));
        r.setRoadAddress(truncate(p.roadAddress(), 500));
        r.setPhone(truncate(p.phone(), 40));
        r.setLatitude(p.latitude());
        r.setLongitude(p.longitude());
    }

    private static String concatNonBlank(String sep, String a, String b) {
        boolean ha = StringUtils.hasText(a);
        boolean hb = StringUtils.hasText(b);
        if (!ha && !hb) {
            return "";
        }
        if (!ha) {
            return b.trim();
        }
        if (!hb) {
            return a.trim();
        }
        if (b.trim().contains(a.trim())) {
            return b.trim();
        }
        return a.trim() + sep + b.trim();
    }

    private static String truncate(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }

    /**
     * 카카오 {@code category_group_code} 우선 처리하고, 그다음 장소명·카테고리 문자열에서 키워드로 분류합니다.
     * 결과는 반드시 {@link RestaurantCategory#ALLOWED_LABELS} 중 하나이며 레거시 DB의 CE7 등은 여기에서 라벨로 바뀝니다.
     */
    static String normalizeCategory(String categoryName, String categoryGroupName, String categoryGroupCode) {
        return normalizeCategory(categoryName, categoryGroupName, categoryGroupCode, null);
    }

    /** @param placeTitle 장소 이름(카카오 {@code place_name} 등)—키워드 추론 보조 */
    static String normalizeCategory(
            String categoryName, String categoryGroupName, String categoryGroupCode, String placeTitle) {
        String resolvedCode = resolveEffectiveGroupCode(categoryGroupCode, categoryName);

        if (!resolvedCode.isEmpty()) {
            String canonical = mapKakaoGroupCode(resolvedCode);
            if (canonical != null) {
                return canonical;
            }
        }

        String keywordSource =
                ((placeTitle == null ? "" : placeTitle)
                                + " "
                                + (categoryName == null ? "" : categoryName)
                                + " "
                                + (categoryGroupName == null ? "" : categoryGroupName)
                                + " "
                                + (categoryGroupCode == null ? "" : categoryGroupCode))
                        .toLowerCase(Locale.ROOT);

        String fromKeywords = inferCategoryFromKeywords(keywordSource);
        if (fromKeywords != null) {
            return fromKeywords;
        }
        return fallbackLabel();
    }

    /**
     * {@code category_group_code} 없이 {@code category_name} 자리에 {@code FD6}·{@code CE7} 만 있는 경우 같은 그룹 코드로 간주합니다.
     */
    private static String resolveEffectiveGroupCode(String categoryGroupCode, String categoryName) {
        if (StringUtils.hasText(categoryGroupCode)) {
            return categoryGroupCode.trim().toUpperCase(Locale.ROOT);
        }
        if (!StringUtils.hasText(categoryName)) {
            return "";
        }
        String candidate = categoryName.trim().toUpperCase(Locale.ROOT);
        return KAKAO_GROUP_CODE_ONLY.matcher(candidate).matches() ? candidate : "";
    }

    /** 키워드·카카오 그룹 코드로 여섯 분류에 맞출 수 없을 때 허용 라벨 {@link RestaurantCategory#ETC} 에 둡니다. */
    private static String fallbackLabel() {
        return RestaurantCategory.ETC;
    }

    /**
     * @return 표준 카테고리 라벨. {@code FD6}(음식점) 및 미등록 코드는 {@code null} 로 두고 키워드 단계 또는 {@link #fallbackLabel()} 처리.
     */
    private static String mapKakaoGroupCode(String code) {
        return switch (code) {
            case "CE7" -> RestaurantCategory.DESSERT; // 카페
            case "FD6" -> null; // 음식점 — 세부는 category_name
            case // 음식점이 아닌 카카오 업종(일정 등에 함께 올린 경우)·백업 데이터용 — 한 탭이라도 들어가게 양식에 둠
                    "MT1",
                    "CS2",
                    "PS3",
                    "SC4",
                    "AC5",
                    "PK6",
                    "OL7",
                    "SW8",
                    "BK9",
                    "CT1",
                    "AG2",
                    "PO3",
                    "AT4",
                    "AD5",
                    "HP8",
                    "PM9" -> RestaurantCategory.WESTERN;
            default -> null;
        };
    }

    private static String inferCategoryFromKeywords(String sourceLower) {
        // 백화점·몰 등 (카카오 카테고리 문자열 우선 처리 — 아래 한식 분식 키워드와 섞이지 않게)
        if (containsAny(
                sourceLower,
                "백화점",
                "대형매장",
                "쇼핑몰",
                "쇼핑센터",
                "쇼핑가",
                "아울렛",
                "department store",
                "shopping mall",
                "shopping center")) {
            return RestaurantCategory.ETC;
        }
        if (containsAny(sourceLower, "디저트", "카페", "베이커리", "빵", "케이크", "도넛", "아이스크림", "커피", "tea")) {
            return RestaurantCategory.DESSERT;
        }
        if (containsAny(sourceLower, "채식", "비건", "샐러드", "vegetarian", "vegan")) {
            return RestaurantCategory.VEGETARIAN;
        }
        if (containsAny(
                sourceLower,
                "일식",
                "일본",
                "초밥",
                "스시",
                "라멘",
                "돈까스",
                "오마카세",
                "이자카야",
                "japanese",
                "udon")) {
            return RestaurantCategory.JAPANESE;
        }
        if (containsAny(sourceLower, "중식", "중국", "짜장", "짬뽕", "마라", "딤섬", "chinese", "dimsum")) {
            return RestaurantCategory.CHINESE;
        }
        if (containsAny(
                sourceLower,
                "양식",
                "서양",
                "이탈",
                "프랑",
                "스테이크",
                "파스타",
                "피자",
                "브런치",
                "햄버거",
                "패스트푸드",
                "패스트 푸드",
                "fast food",
                "fastfood",
                "burger",
                "멕시",
                "타코",
                "western",
                "italian",
                "french",
                "american",
                "steakhouse",
                "brunch")) {
            return RestaurantCategory.WESTERN;
        }
        if (containsAny(
                sourceLower,
                "한식",
                "국밥",
                "찌개",
                "분식",
                "보쌈",
                "족발",
                "막창",
                "곱창",
                "닭발",
                "치킨",
                "통닭",
                "포차",
                "술집",
                "호프",
                " pub",
                " bar",
                "korean bbq")) {
            return RestaurantCategory.KOREAN;
        }
        if (containsAny(sourceLower, "korean")) {
            return RestaurantCategory.KOREAN;
        }
        return null;
    }

    private static boolean containsAny(String source, String... keywords) {
        for (String keyword : keywords) {
            if (source.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
