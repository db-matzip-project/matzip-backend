package com.example.dbmatzip.domain.schedule.repository;

import com.example.dbmatzip.domain.schedule.entity.ScheduleRestaurant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleRestaurantRepository extends JpaRepository<ScheduleRestaurant, Long> {

    boolean existsBySchedule_IdAndRestaurant_Id(Long scheduleId, Long restaurantId);

    @Query(
            "select coalesce(max(sr.visitOrder), 0) from ScheduleRestaurant sr where sr.schedule.id = :scheduleId")
    int findMaxVisitOrder(@Param("scheduleId") Long scheduleId);

    List<ScheduleRestaurant> findBySchedule_IdOrderByVisitOrderAsc(Long scheduleId);

    Optional<ScheduleRestaurant> findByIdAndSchedule_Id(Long itemId, Long scheduleId);
}
