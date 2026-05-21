package com.example.dbmatzip.domain.schedule.controller;

import com.example.dbmatzip.domain.schedule.dto.AddScheduleItemRequest;
import com.example.dbmatzip.domain.schedule.dto.ReorderScheduleItemsRequest;
import com.example.dbmatzip.domain.schedule.dto.ScheduleCreateRequest;
import com.example.dbmatzip.domain.schedule.dto.ScheduleDetailResponse;
import com.example.dbmatzip.domain.schedule.dto.ScheduleSummaryResponse;
import com.example.dbmatzip.domain.schedule.dto.ScheduleUpdateRequest;
import com.example.dbmatzip.domain.schedule.service.ScheduleService;
import com.example.dbmatzip.global.security.MemberPrincipal;
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
    public ScheduleDetailResponse addItem(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody AddScheduleItemRequest request) {
        return scheduleService.addRestaurant(scheduleId, principal.getId(), request);
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
