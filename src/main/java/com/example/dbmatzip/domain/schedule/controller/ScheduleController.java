package com.example.dbmatzip.domain.schedule.controller;

import com.example.dbmatzip.domain.schedule.dto.AddScheduleItemRequest;
import com.example.dbmatzip.domain.schedule.dto.AddSchedulePlaceItemRequest;
import com.example.dbmatzip.domain.schedule.dto.ReorderScheduleItemsRequest;
import com.example.dbmatzip.domain.schedule.dto.ScheduleCreateRequest;
import com.example.dbmatzip.domain.schedule.dto.ScheduleDetailResponse;
import com.example.dbmatzip.domain.schedule.dto.ScheduleSummaryResponse;
import com.example.dbmatzip.domain.schedule.dto.ScheduleUpdateRequest;
import com.example.dbmatzip.domain.schedule.service.ScheduleService;
import com.example.dbmatzip.global.security.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "일정 생성",
            description =
                    "일정 헤더를 생성합니다. 요청에 restaurantIds를 함께 보내면 schedule_restaurants 항목이 visitOrder 1부터 함께 생성됩니다.")
    public ScheduleDetailResponse create(
            @Valid @RequestBody ScheduleCreateRequest request, @AuthenticationPrincipal MemberPrincipal principal) {
        return scheduleService.create(request, principal.getId());
    }

    @GetMapping
    public List<ScheduleSummaryResponse> list(@AuthenticationPrincipal MemberPrincipal principal) {
        return scheduleService.listByUser(principal.getId());
    }

    @GetMapping("/{scheduleId}")
    public ScheduleDetailResponse detail(
            @PathVariable Long scheduleId, @AuthenticationPrincipal MemberPrincipal principal) {
        return scheduleService.getDetail(scheduleId, principal.getId());
    }

    @PatchMapping("/{scheduleId}")
    public ScheduleDetailResponse update(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody ScheduleUpdateRequest request) {
        return scheduleService.update(scheduleId, principal.getId(), request);
    }

    @DeleteMapping("/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long scheduleId, @AuthenticationPrincipal MemberPrincipal principal) {
        scheduleService.delete(scheduleId, principal.getId());
    }

    @PostMapping("/{scheduleId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "일정에 식당 추가", description = "기존 일정에 식당을 추가하고 visitOrder를 마지막 순서로 배정합니다.")
    public ScheduleDetailResponse addItem(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody AddScheduleItemRequest request) {
        return scheduleService.addRestaurant(scheduleId, principal.getId(), request);
    }

    @PostMapping("/{scheduleId}/items/from-place")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "일정에 장소 추가(upsert)",
            description =
                    "프론트에서 카카오 장소 검색 등으로 받은 정보를 넘기면 api_id 기준으로 restaurants 행을 "
                            + "없으면 생성·있으면 갱신한 뒤 일정 항목으로 연결합니다. 좌표는 latitude·longitude 에 저장합니다.")
    public ScheduleDetailResponse addItemFromExternalPlace(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody AddSchedulePlaceItemRequest request) {
        return scheduleService.addRestaurantFromExternalPlace(scheduleId, principal.getId(), request);
    }

    @DeleteMapping("/{scheduleId}/items/{itemId}")
    public ScheduleDetailResponse removeItem(
            @PathVariable Long scheduleId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal MemberPrincipal principal) {
        return scheduleService.removeItem(scheduleId, principal.getId(), itemId);
    }

    @PutMapping("/{scheduleId}/items/order")
    public ScheduleDetailResponse reorder(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody ReorderScheduleItemsRequest request) {
        return scheduleService.reorderItems(scheduleId, principal.getId(), request);
    }
}
