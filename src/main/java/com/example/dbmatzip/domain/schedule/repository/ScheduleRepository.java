package com.example.dbmatzip.domain.schedule.repository;

import com.example.dbmatzip.domain.schedule.dto.ScheduleSummaryResponse;
import com.example.dbmatzip.domain.schedule.entity.Schedule;
import com.example.dbmatzip.domain.schedule.entity.ScheduleRestaurant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query(
            """
            select new com.example.dbmatzip.domain.schedule.dto.ScheduleSummaryResponse(
              s.id, s.title, s.travelDate, s.createdAt,
              (select count(i.id) from ScheduleRestaurant i where i.schedule.id = s.id)
            )
            from Schedule s
            where s.userId = :userId
            order by s.createdAt desc
            """)
    List<ScheduleSummaryResponse> findSummariesByUserId(@Param("userId") Long userId);

    @Query(
            """
            select distinct s from Schedule s
            left join fetch s.items i
            left join fetch i.restaurant
            where s.id = :id
            """)
    Optional<Schedule> findByIdWithItems(@Param("id") Long id);

    Optional<Schedule> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);
}
