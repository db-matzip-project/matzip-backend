package com.example.dbmatzip.domain.restaurant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 식당 테이블. 공간 검색은 위경도 컬럼 + PostGIS ST_MakePoint 로 처리합니다.
 */
@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_id", unique = true, length = 64)
    private String apiId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 80)
    private String category;

    @Column(length = 500)
    private String address;

    @Column(length = 500)
    private String roadAddress;

    @Column(length = 40)
    private String phone;

    @Column(length = 2000)
    private String description;

    private Double latitude;

    private Double longitude;

    private Double rating;

    private Integer reviewCount;

    /** 기존 행이 있을 때 NOT NULL 추가 실패 방지: DB 기본값 0 */
    @Column(name = "schedule_add_count", nullable = false)
    @ColumnDefault("0")
    private Integer scheduleAddCount = 0;
}
