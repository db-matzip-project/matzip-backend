package com.example.dbmatzip.domain.schedule.entity;

import com.example.dbmatzip.domain.member.entity.User;
import com.example.dbmatzip.domain.restaurant.entity.Restaurant;
import com.example.dbmatzip.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "schedules")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(name = "visit_date_time", nullable = false)
    private LocalDateTime visitDateTime;

    @Builder
    private Schedule(User user, Restaurant restaurant, LocalDateTime visitDateTime) {
        this.user = user;
        this.restaurant = restaurant;
        this.visitDateTime = visitDateTime;
    }
}
