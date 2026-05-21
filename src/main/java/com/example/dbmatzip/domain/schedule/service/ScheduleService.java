package com.example.dbmatzip.domain.schedule.service;

import com.example.dbmatzip.domain.restaurant.dto.RestaurantResponse;
import com.example.dbmatzip.domain.restaurant.entity.Restaurant;
import com.example.dbmatzip.domain.restaurant.exception.RestaurantNotFoundException;
import com.example.dbmatzip.domain.restaurant.repository.RestaurantRepository;
import com.example.dbmatzip.domain.schedule.dto.AddScheduleItemRequest;
import com.example.dbmatzip.domain.schedule.dto.ReorderScheduleItemsRequest;
import com.example.dbmatzip.domain.schedule.dto.ScheduleCreateRequest;
import com.example.dbmatzip.domain.schedule.dto.ScheduleDetailResponse;
import com.example.dbmatzip.domain.schedule.dto.ScheduleDetailResponse.ScheduleItemResponse;
import com.example.dbmatzip.domain.schedule.dto.ScheduleSummaryResponse;
import com.example.dbmatzip.domain.schedule.entity.Schedule;
import com.example.dbmatzip.domain.schedule.entity.ScheduleRestaurant;
import com.example.dbmatzip.domain.schedule.exception.ScheduleNotFoundException;
import com.example.dbmatzip.domain.schedule.repository.ScheduleRepository;
import com.example.dbmatzip.domain.schedule.repository.ScheduleRestaurantRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleRestaurantRepository scheduleRestaurantRepository;
    private final RestaurantRepository restaurantRepository;

    /** 일정 생성 */
    @Transactional
    public ScheduleDetailResponse create(ScheduleCreateRequest request) {
        Schedule schedule = new Schedule();
        schedule.setUserId(request.userId());
        schedule.setTitle(request.title());
        schedule.setTravelDate(request.travelDate());
        scheduleRepository.save(schedule);
        return getDetail(schedule.getId(), request.userId());
    }

    /** 사용자별 일정 목록(개수 포함) */
    public List<ScheduleSummaryResponse> listByUser(Long userId) {
        return scheduleRepository.findSummariesByUserId(userId);
    }

    /** 일정 상세(식당·순서 포함) — 소유자 userId 검증 */
    public ScheduleDetailResponse getDetail(Long scheduleId, Long userId) {
        Schedule schedule =
                scheduleRepository.findByIdWithItems(scheduleId).orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
        if (!schedule.getUserId().equals(userId)) {
            throw new IllegalArgumentException("해당 일정에 접근할 수 없습니다.");
        }
        List<ScheduleItemResponse> items = schedule.getItems().stream()
                .sorted((a, b) -> Integer.compare(a.getVisitOrder(), b.getVisitOrder()))
                .map(sr -> new ScheduleItemResponse(
                        sr.getId(), sr.getVisitOrder(), sr.getMemo(), RestaurantResponse.from(sr.getRestaurant())))
                .toList();
        return new ScheduleDetailResponse(
                schedule.getId(), schedule.getTitle(), schedule.getTravelDate(), schedule.getCreatedAt(), items);
    }

    /** 일정 삭제 */
    @Transactional
    public void delete(Long scheduleId, Long userId) {
        Schedule schedule = scheduleRepository
                .findByIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
        scheduleRepository.delete(schedule);
    }

    /** 일정에 식당 추가 — 트리거로 restaurants.schedule_add_count 증가(트리거 적용 시). */
    @Transactional
    public ScheduleDetailResponse addRestaurant(Long scheduleId, Long userId, AddScheduleItemRequest request) {
        scheduleRepository
                .findByIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
        if (scheduleRestaurantRepository.existsBySchedule_IdAndRestaurant_Id(scheduleId, request.restaurantId())) {
            throw new IllegalArgumentException("이미 일정에 담긴 식당입니다.");
        }
        Restaurant restaurant = restaurantRepository
                .findById(request.restaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(request.restaurantId()));

        ScheduleRestaurant row = new ScheduleRestaurant();
        row.setSchedule(scheduleRepository.getReferenceById(scheduleId));
        row.setRestaurant(restaurant);
        row.setMemo(request.memo());
        row.setVisitOrder(scheduleRestaurantRepository.findMaxVisitOrder(scheduleId) + 1);
        scheduleRestaurantRepository.save(row);

        return getDetail(scheduleId, userId);
    }

    /** 일정에서 식당 한 줄 삭제 후 visit_order 재정렬 */
    @Transactional
    public ScheduleDetailResponse removeItem(Long scheduleId, Long userId, Long itemId) {
        scheduleRepository
                .findByIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
        ScheduleRestaurant row = scheduleRestaurantRepository
                .findByIdAndSchedule_Id(itemId, scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정 항목을 찾을 수 없습니다."));
        scheduleRestaurantRepository.delete(row);
        normalizeVisitOrders(scheduleId);
        return getDetail(scheduleId, userId);
    }

    /**
     * 방문 순서 일괄 변경. 검증 실패 시 예외 → 전체 롤백(@Transactional).
     */
    @Transactional
    public ScheduleDetailResponse reorderItems(Long scheduleId, Long userId, ReorderScheduleItemsRequest request) {
        scheduleRepository
                .findByIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));

        List<ScheduleRestaurant> rows =
                scheduleRestaurantRepository.findBySchedule_IdOrderByVisitOrderAsc(scheduleId);
        if (rows.size() != request.orderedRestaurantIds().size()) {
            throw new IllegalArgumentException(
                    "순서 목록 길이(%d)가 일정 항목 수(%d)와 같아야 합니다."
                            .formatted(request.orderedRestaurantIds().size(), rows.size()));
        }

        var idsInDb = rows.stream().map(sr -> sr.getRestaurant().getId()).collect(Collectors.toSet());
        var idsReq = new HashSet<>(request.orderedRestaurantIds());
        if (!idsInDb.equals(idsReq)) {
            throw new IllegalArgumentException("순서 목록은 현재 일정에 담긴 식당 ID 집합과 정확히 일치해야 합니다.");
        }

        Map<Long, ScheduleRestaurant> byRestaurantId = rows.stream()
                .collect(Collectors.toMap(sr -> sr.getRestaurant().getId(), sr -> sr));
        for (int i = 0; i < request.orderedRestaurantIds().size(); i++) {
            Long rid = Objects.requireNonNull(request.orderedRestaurantIds().get(i));
            ScheduleRestaurant sr = byRestaurantId.get(rid);
            if (sr == null) {
                throw new IllegalStateException("매핑 행을 찾을 수 없습니다. restaurantId=" + rid);
            }
            sr.setVisitOrder(i + 1);
        }
        scheduleRestaurantRepository.saveAll(rows);
        return getDetail(scheduleId, userId);
    }

    private void normalizeVisitOrders(Long scheduleId) {
        List<ScheduleRestaurant> rows =
                scheduleRestaurantRepository.findBySchedule_IdOrderByVisitOrderAsc(scheduleId);
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setVisitOrder(i + 1);
        }
        scheduleRestaurantRepository.saveAll(rows);
    }
}
