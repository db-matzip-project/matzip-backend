package com.example.dbmatzip.domain.schedule.entity;

import com.example.dbmatzip.domain.restaurant.entity.Restaurant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 일정–식당 매핑 테이블(schedule_restaurants). */
@Entity
@Table(
        name = "schedule_restaurants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"schedule_id", "restaurant_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ScheduleRestaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(name = "visit_order", nullable = false)
    private int visitOrder;

    @Column(length = 500)
    private String memo;
}
