package com.example.dbmatzip.domain.restaurant.controller;

import com.example.dbmatzip.domain.restaurant.dto.KakaoImportRequest;
import com.example.dbmatzip.domain.restaurant.dto.KakaoImportResponse;
import com.example.dbmatzip.domain.restaurant.dto.PageResponse;
import com.example.dbmatzip.domain.restaurant.dto.RestaurantResponse;
import com.example.dbmatzip.domain.restaurant.dto.UpsertRestaurantFromPlacePayload;
import com.example.dbmatzip.domain.restaurant.service.KakaoRestaurantImportService;
import com.example.dbmatzip.domain.restaurant.service.RestaurantService;
import com.example.dbmatzip.domain.restaurant.service.RestaurantUpsertService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final RestaurantUpsertService restaurantUpsertService;
    private final KakaoRestaurantImportService kakaoRestaurantImportService;
    private final Validator validator;

    /**
     * Boot 4 는 MVC 기본 Mapper 가 {@code com.fasterxml.jackson.databind.ObjectMapper} 빈으로 노출되지 않을 수 있어,
     * from-place 매핑만 전용 인스턴스로 처리합니다.
     */
    private static final ObjectMapper FROM_PLACE_JSON_MAPPER = new ObjectMapper();

    /**
     * tasteSimilar=true 이면 Authorization: Bearer JWT 필요. 입맛 비슷한 사용자들이 최근 일정에 많이 담은 식당 ID 와 bounds 교집합.
     */
    @GetMapping
    public PageResponse<RestaurantResponse> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double minLat,
            @RequestParam(required = false) Double minLng,
            @RequestParam(required = false) Double maxLat,
            @RequestParam(required = false) Double maxLng,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "false") boolean tasteSimilar,
            Authentication authentication) {
        return restaurantService.search(
                category,
                minRating,
                minLat,
                minLng,
                maxLat,
                maxLng,
                page,
                size,
                sortBy,
                sort,
                tasteSimilar,
                authentication);
    }

    @GetMapping("/{id}")
    public RestaurantResponse detail(@PathVariable Long id) {
        return restaurantService.getById(id);
    }

    /** 로그인한 사용자가 카카오 장소 JSON으로 식당을 api_id 기준 upsert 할 때 사용합니다. JWT 필수. */
    @PostMapping("/from-place")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "장소 정보로 식당 upsert",
            description =
                    "`schedules/.../items/from-place` 와 같은 `{ \"place\": { ... } }` 또는 장소 필드를 루트에 두는 형식 모두 허용. JWT 필수. 응답은 저장된 레스토랑입니다.")
    public RestaurantResponse upsertFromPlace(@RequestBody JsonNode body) {
        UpsertRestaurantFromPlacePayload payload = parseUpsertRestaurantFromPlacePayload(body);
        var violations = validator.validate(payload);
        if (!violations.isEmpty()) {
            String msg = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException(msg);
        }
        return RestaurantResponse.from(restaurantUpsertService.upsertFromExternalPlace(payload));
    }

    /** 루트에 장소 필드 두기 또는 일정용과 같이 `{ \"place\": {...} }` 형태 모두 허용. */
    private UpsertRestaurantFromPlacePayload parseUpsertRestaurantFromPlacePayload(JsonNode body) {
        if (body == null || body.isNull()) {
            throw new IllegalArgumentException("요청 본문이 비어 있습니다.");
        }
        JsonNode node =
                body.hasNonNull("place") ? body.get("place") : body;
        try {
            return FROM_PLACE_JSON_MAPPER.treeToValue(node, UpsertRestaurantFromPlacePayload.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("place 페이로드 형식을 읽지 못했습니다.", e);
        }
    }

    @PostMapping("/import/kakao")
    public KakaoImportResponse importFromKakao(@Valid @RequestBody KakaoImportRequest request) {
        return kakaoRestaurantImportService.importFromKeyword(request);
    }
}
