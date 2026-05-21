package com.example.dbmatzip.domain.route.service;

import com.example.dbmatzip.domain.restaurant.entity.Restaurant;
import com.example.dbmatzip.domain.restaurant.repository.RestaurantRepository;
import com.example.dbmatzip.domain.route.dto.RouteLegResponse;
import com.example.dbmatzip.domain.route.dto.SuggestedVisitOrderResponse;
import com.example.dbmatzip.domain.route.support.GeoDistance;
import com.example.dbmatzip.domain.schedule.entity.Schedule;
import com.example.dbmatzip.domain.schedule.entity.ScheduleRestaurant;
import com.example.dbmatzip.domain.schedule.exception.ScheduleNotFoundException;
import com.example.dbmatzip.domain.schedule.repository.ScheduleRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteOptimizationService {

    private final ScheduleRepository scheduleRepository;
    private final RestaurantRepository restaurantRepository;

    public List<RouteLegResponse> legsForSchedule(Long scheduleId, Long userId) {
        Schedule schedule =
                scheduleRepository.findByIdWithItems(scheduleId).orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
        assertOwner(schedule, userId);
        List<ScheduleRestaurant> ordered = schedule.getItems().stream()
                .sorted(Comparator.comparingInt(ScheduleRestaurant::getVisitOrder))
                .toList();
        List<RouteLegResponse> legs = new ArrayList<>();
        for (int i = 0; i < ordered.size() - 1; i++) {
            Restaurant a = ordered.get(i).getRestaurant();
            Restaurant b = ordered.get(i + 1).getRestaurant();
            legs.add(new RouteLegResponse(
                    a.getId(),
                    b.getId(),
                    GeoDistance.km(coords(a)[0], coords(a)[1], coords(b)[0], coords(b)[1])));
        }
        return legs;
    }

    /** 저장하지 않고 근사 최단 방문 순서만 제안(그리디 최근접 이웃). */
    public SuggestedVisitOrderResponse suggestOrderForSchedule(Long scheduleId, Long userId, Long startRestaurantId) {
        Schedule schedule =
                scheduleRepository.findByIdWithItems(scheduleId).orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
        assertOwner(schedule, userId);
        List<Restaurant> rests =
                schedule.getItems().stream()
                        .sorted(Comparator.comparingInt(ScheduleRestaurant::getVisitOrder))
                        .map(ScheduleRestaurant::getRestaurant)
                        .distinct()
                        .toList();
        if (rests.size() <= 1) {
            List<Long> ids = rests.stream().map(Restaurant::getId).toList();
            return new SuggestedVisitOrderResponse(ids, 0d);
        }
        return nearestNeighborOrder(rests, startRestaurantId);
    }

    public SuggestedVisitOrderResponse suggestOrder(List<Long> restaurantIds, Long startRestaurantId) {
        Set<Long> uniq = new HashSet<>(restaurantIds);
        if (uniq.size() != restaurantIds.size()) {
            throw new IllegalArgumentException("restaurantIds 에 중복이 있습니다.");
        }
        List<Restaurant> rests = restaurantRepository.findAllById(restaurantIds);
        if (rests.size() != restaurantIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 식당 ID 가 포함되어 있습니다.");
        }
        Map<Long, Restaurant> byId = rests.stream().collect(Collectors.toMap(Restaurant::getId, r -> r));
        List<Restaurant> orderedList = restaurantIds.stream().map(byId::get).toList();
        if (orderedList.size() <= 1) {
            return new SuggestedVisitOrderResponse(restaurantIds, 0d);
        }
        return nearestNeighborOrder(orderedList, startRestaurantId);
    }

    private SuggestedVisitOrderResponse nearestNeighborOrder(List<Restaurant> restaurants, Long startRestaurantId) {
        List<Restaurant> remaining = new ArrayList<>(restaurants);
        Restaurant current = pickStart(remaining, startRestaurantId);
        List<Long> visitOrder = new ArrayList<>();
        visitOrder.add(current.getId());
        remaining.remove(current);
        double totalKm = 0;
        while (!remaining.isEmpty()) {
            final Restaurant from = current;
            Restaurant next = remaining.stream()
                    .min(Comparator.comparingDouble(
                            r -> GeoDistance.km(coords(from)[0], coords(from)[1], coords(r)[0], coords(r)[1])))
                    .orElseThrow();
            totalKm += GeoDistance.km(coords(from)[0], coords(from)[1], coords(next)[0], coords(next)[1]);
            visitOrder.add(next.getId());
            remaining.remove(next);
            current = next;
        }
        return new SuggestedVisitOrderResponse(visitOrder, Math.round(totalKm * 1000d) / 1000d);
    }

    private static Restaurant pickStart(List<Restaurant> remaining, Long startRestaurantId) {
        if (startRestaurantId != null) {
            return remaining.stream()
                    .filter(r -> r.getId().equals(startRestaurantId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("startRestaurantId 가 목록에 없습니다."));
        }
        return remaining.stream()
                .max(Comparator.comparingDouble(r -> r.getLatitude() == null ? Double.NEGATIVE_INFINITY : r.getLatitude()))
                .orElseThrow();
    }

    private static double[] coords(Restaurant r) {
        if (r.getLatitude() == null || r.getLongitude() == null) {
            throw new IllegalArgumentException("식당 좌표가 없습니다. id=" + r.getId());
        }
        return new double[] {r.getLatitude(), r.getLongitude()};
    }

    private static void assertOwner(Schedule schedule, Long userId) {
        if (!schedule.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 일정에 접근할 수 없습니다.");
        }
    }
}
