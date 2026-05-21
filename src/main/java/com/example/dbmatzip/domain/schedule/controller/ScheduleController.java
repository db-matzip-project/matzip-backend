package com.example.dbmatzip.domain.schedule.controller;

import com.example.dbmatzip.domain.schedule.dto.AddScheduleItemRequest;
import com.example.dbmatzip.domain.schedule.dto.ReorderScheduleItemsRequest;
import com.example.dbmatzip.domain.schedule.dto.ScheduleCreateRequest;
import com.example.dbmatzip.domain.schedule.dto.ScheduleDetailResponse;
import com.example.dbmatzip.domain.schedule.dto.ScheduleSummaryResponse;
import com.example.dbmatzip.domain.schedule.service.ScheduleService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    /** 일정 생성 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleDetailResponse create(@Valid @RequestBody ScheduleCreateRequest request) {
        return scheduleService.create(request);
    }

    /** 일정 목록 조회 */
    @GetMapping
    public List<ScheduleSummaryResponse> list(@RequestParam Long userId) {
        return scheduleService.listByUser(userId);
    }

    /** 일정 상세 조회 */
    @GetMapping("/{scheduleId}")
    public ScheduleDetailResponse detail(@PathVariable Long scheduleId, @RequestParam Long userId) {
        return scheduleService.getDetail(scheduleId, userId);
    }

    /** 일정 삭제 */
    @DeleteMapping("/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long scheduleId, @RequestParam Long userId) {
        scheduleService.delete(scheduleId, userId);
    }

    /** 일정에 식당 추가 */
    @PostMapping("/{scheduleId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleDetailResponse addItem(
            @PathVariable Long scheduleId,
            @RequestParam Long userId,
            @Valid @RequestBody AddScheduleItemRequest request) {
        return scheduleService.addRestaurant(scheduleId, userId, request);
    }

    /** 일정에서 항목 삭제 */
    @DeleteMapping("/{scheduleId}/items/{itemId}")
    public ScheduleDetailResponse removeItem(
            @PathVariable Long scheduleId, @PathVariable Long itemId, @RequestParam Long userId) {
        return scheduleService.removeItem(scheduleId, userId, itemId);
    }

    /** 방문 순서 일괄 변경(단일 트랜잭션·무결성 검증) */
    @PutMapping("/{scheduleId}/items/order")
    public ScheduleDetailResponse reorder(
            @PathVariable Long scheduleId,
            @RequestParam Long userId,
            @Valid @RequestBody ReorderScheduleItemsRequest request) {
        return scheduleService.reorderItems(scheduleId, userId, request);
    }
}
