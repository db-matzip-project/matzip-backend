package com.example.dbmatzip.domain.restaurant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 식당. 핵심 스키마는 팀 ERD( api_id, 이름, 카테고리, 주소, 위경도, 평점 )와 동일하며,
 * 카카오 연동·상세 화면·향후 통계를 위해 필드를 확장했습니다. — {@code docs/erd.md} 참고.
 * 공간 검색: PostGIS {@code ST_MakePoint(longitude, latitude)}.
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

    /** 도로명 주소 (카카오 등). 상세·지도 연동 시 표시용. */
    @Column(length = 500)
    private String roadAddress;

    @Column(length = 40)
    private String phone;

    /**
     * 부가 설명. 카카오 키워드 검색의 category_name(업종 경로) 등을 저장.
     * 팀에서 별도 소개 문구를 넣어도 됨.
     */
    @Column(length = 2000)
    private String description;

    private Double latitude;

    private Double longitude;

    private Double rating;

    /**
     * 리뷰 수 / 방문·찜 집계 등 (외부 API 또는 옵션). 없으면 null.
     */
    private Integer reviewCount;

    /**
     * 일정에 담긴 총 횟수 — DB 트리거로 증가 ({@code db/schedule-restaurant-trigger.sql}).
     */
    @Column(name = "schedule_add_count", nullable = false)
    private Integer scheduleAddCount = 0;
}
