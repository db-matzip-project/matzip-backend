package com.example.dbmatzip.domain.restaurant.repository;

import com.example.dbmatzip.domain.restaurant.entity.Restaurant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByApiId(String apiId);

    /**
     * 지도 bounds(min/max 위·경도) 안의 식당 — PostGIS {@code ST_Contains(Envelope, Point)},
     * {@code ST_MakeEnvelope(minLng,minLat,maxLng,maxLat,4326)} + 표현식 GIST 인덱스({@code db/postgis.sql}).
     */
    @Query(
            value =
                    """
                    SELECT r.* FROM restaurants r
                    WHERE (
                        :category IS NULL
                        OR r.category = :category
                        OR r.category ILIKE CONCAT('%', :category, '%')
                        OR r.description ILIKE CONCAT('%', :category, '%')
                        OR (:category = '한식' AND r.category ILIKE '%korean%')
                        OR (:category = '일식' AND r.category ILIKE '%japanese%')
                        OR (:category = '중식' AND r.category ILIKE '%chinese%')
                        OR (:category = '양식' AND (r.category ILIKE '%western%' OR r.category ILIKE '%italian%' OR r.category ILIKE '%french%'))
                        OR (:category = '채식' AND (r.category ILIKE '%vegetarian%' OR r.category ILIKE '%vegan%'))
                        OR (:category = '디저트' AND (
                            r.category ILIKE '%dessert%' OR r.category ILIKE '%cafe%' OR r.category ILIKE '%bakery%'
                            OR trim(upper(r.category)) IN ('CE7')
                            OR r.description ILIKE '%카페%'))
                    )
                    AND (:minRating IS NULL OR r.rating >= :minRating)
                    AND (
                        :minLat IS NULL
                        OR ST_Contains(
                            ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326),
                            ST_SetSRID(ST_MakePoint(r.longitude, r.latitude), 4326)
                        )
                    )
                    ORDER BY
                        CASE WHEN :sortBy = 'rating_asc' THEN r.rating END ASC NULLS LAST,
                        CASE WHEN :sortBy = 'rating_desc' THEN r.rating END DESC NULLS LAST,
                        CASE WHEN :sortBy = 'review_count_asc' THEN r.review_count END ASC NULLS LAST,
                        CASE WHEN :sortBy = 'review_count_desc' THEN r.review_count END DESC NULLS LAST,
                        r.id ASC
                    """,
            countQuery =
                    """
                    SELECT count(*) FROM restaurants r
                    WHERE (
                        :category IS NULL
                        OR r.category = :category
                        OR r.category ILIKE CONCAT('%', :category, '%')
                        OR r.description ILIKE CONCAT('%', :category, '%')
                        OR (:category = '한식' AND r.category ILIKE '%korean%')
                        OR (:category = '일식' AND r.category ILIKE '%japanese%')
                        OR (:category = '중식' AND r.category ILIKE '%chinese%')
                        OR (:category = '양식' AND (r.category ILIKE '%western%' OR r.category ILIKE '%italian%' OR r.category ILIKE '%french%'))
                        OR (:category = '채식' AND (r.category ILIKE '%vegetarian%' OR r.category ILIKE '%vegan%'))
                        OR (:category = '디저트' AND (
                            r.category ILIKE '%dessert%' OR r.category ILIKE '%cafe%' OR r.category ILIKE '%bakery%'
                            OR trim(upper(r.category)) IN ('CE7')
                            OR r.description ILIKE '%카페%'))
                    )
                    AND (:minRating IS NULL OR r.rating >= :minRating)
                    AND (
                        :minLat IS NULL
                        OR ST_Contains(
                            ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326),
                            ST_SetSRID(ST_MakePoint(r.longitude, r.latitude), 4326)
                        )
                    )
                    """,
            nativeQuery = true)
    Page<Restaurant> search(
            @Param("category") String category,
            @Param("minRating") Double minRating,
            @Param("minLat") Double minLat,
            @Param("minLng") Double minLng,
            @Param("maxLat") Double maxLat,
            @Param("maxLng") Double maxLng,
            @Param("sortBy") String sortBy,
            Pageable pageable);

    /** 입맛 비슷한 사용자 추천 식당 ID 집합과 bounds 검색 교집합 */
    @Query(
            value =
                    """
                    SELECT r.* FROM restaurants r
                    WHERE r.id IN (:ids)
                    AND (
                        :category IS NULL
                        OR r.category = :category
                        OR r.category ILIKE CONCAT('%', :category, '%')
                        OR r.description ILIKE CONCAT('%', :category, '%')
                        OR (:category = '한식' AND r.category ILIKE '%korean%')
                        OR (:category = '일식' AND r.category ILIKE '%japanese%')
                        OR (:category = '중식' AND r.category ILIKE '%chinese%')
                        OR (:category = '양식' AND (r.category ILIKE '%western%' OR r.category ILIKE '%italian%' OR r.category ILIKE '%french%'))
                        OR (:category = '채식' AND (r.category ILIKE '%vegetarian%' OR r.category ILIKE '%vegan%'))
                        OR (:category = '디저트' AND (
                            r.category ILIKE '%dessert%' OR r.category ILIKE '%cafe%' OR r.category ILIKE '%bakery%'
                            OR trim(upper(r.category)) IN ('CE7')
                            OR r.description ILIKE '%카페%'))
                    )
                    AND (:minRating IS NULL OR r.rating >= :minRating)
                    AND (
                        :minLat IS NULL
                        OR ST_Contains(
                            ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326),
                            ST_SetSRID(ST_MakePoint(r.longitude, r.latitude), 4326)
                        )
                    )
                    ORDER BY
                        CASE WHEN :sortBy = 'rating_asc' THEN r.rating END ASC NULLS LAST,
                        CASE WHEN :sortBy = 'rating_desc' THEN r.rating END DESC NULLS LAST,
                        CASE WHEN :sortBy = 'review_count_asc' THEN r.review_count END ASC NULLS LAST,
                        CASE WHEN :sortBy = 'review_count_desc' THEN r.review_count END DESC NULLS LAST,
                        r.id ASC
                    """,
            countQuery =
                    """
                    SELECT count(*) FROM restaurants r
                    WHERE r.id IN (:ids)
                    AND (
                        :category IS NULL
                        OR r.category = :category
                        OR r.category ILIKE CONCAT('%', :category, '%')
                        OR r.description ILIKE CONCAT('%', :category, '%')
                        OR (:category = '한식' AND r.category ILIKE '%korean%')
                        OR (:category = '일식' AND r.category ILIKE '%japanese%')
                        OR (:category = '중식' AND r.category ILIKE '%chinese%')
                        OR (:category = '양식' AND (r.category ILIKE '%western%' OR r.category ILIKE '%italian%' OR r.category ILIKE '%french%'))
                        OR (:category = '채식' AND (r.category ILIKE '%vegetarian%' OR r.category ILIKE '%vegan%'))
                        OR (:category = '디저트' AND (
                            r.category ILIKE '%dessert%' OR r.category ILIKE '%cafe%' OR r.category ILIKE '%bakery%'
                            OR trim(upper(r.category)) IN ('CE7')
                            OR r.description ILIKE '%카페%'))
                    )
                    AND (:minRating IS NULL OR r.rating >= :minRating)
                    AND (
                        :minLat IS NULL
                        OR ST_Contains(
                            ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326),
                            ST_SetSRID(ST_MakePoint(r.longitude, r.latitude), 4326)
                        )
                    )
                    """,
            nativeQuery = true)
    Page<Restaurant> searchAmongIds(
            @Param("ids") java.util.Collection<Long> ids,
            @Param("category") String category,
            @Param("minRating") Double minRating,
            @Param("minLat") Double minLat,
            @Param("minLng") Double minLng,
            @Param("maxLat") Double maxLat,
            @Param("maxLng") Double maxLng,
            @Param("sortBy") String sortBy,
            Pageable pageable);

    @Query(
            value =
                    """
                    SELECT r.* FROM restaurants r
                    WHERE (
                        :category IS NULL
                        OR r.category = :category
                        OR r.category ILIKE CONCAT('%', :category, '%')
                        OR r.description ILIKE CONCAT('%', :category, '%')
                        OR (:category = '한식' AND r.category ILIKE '%korean%')
                        OR (:category = '일식' AND r.category ILIKE '%japanese%')
                        OR (:category = '중식' AND r.category ILIKE '%chinese%')
                        OR (:category = '양식' AND (r.category ILIKE '%western%' OR r.category ILIKE '%italian%' OR r.category ILIKE '%french%'))
                        OR (:category = '채식' AND (r.category ILIKE '%vegetarian%' OR r.category ILIKE '%vegan%'))
                        OR (:category = '디저트' AND (
                            r.category ILIKE '%dessert%' OR r.category ILIKE '%cafe%' OR r.category ILIKE '%bakery%'
                            OR trim(upper(r.category)) IN ('CE7')
                            OR r.description ILIKE '%카페%'))
                    )
                    AND (:minRating IS NULL OR r.rating >= :minRating)
                    AND (
                        :minLat IS NULL
                        OR ST_Contains(
                            ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326),
                            ST_SetSRID(ST_MakePoint(r.longitude, r.latitude), 4326)
                        )
                    )
                    ORDER BY
                        (ABS(COALESCE(r.latitude, :centerLat) - :centerLat) + ABS(COALESCE(r.longitude, :centerLng) - :centerLng)) ASC,
                        r.id ASC
                    """,
            countQuery =
                    """
                    SELECT count(*) FROM restaurants r
                    WHERE (
                        :category IS NULL
                        OR r.category = :category
                        OR r.category ILIKE CONCAT('%', :category, '%')
                        OR r.description ILIKE CONCAT('%', :category, '%')
                        OR (:category = '한식' AND r.category ILIKE '%korean%')
                        OR (:category = '일식' AND r.category ILIKE '%japanese%')
                        OR (:category = '중식' AND r.category ILIKE '%chinese%')
                        OR (:category = '양식' AND (r.category ILIKE '%western%' OR r.category ILIKE '%italian%' OR r.category ILIKE '%french%'))
                        OR (:category = '채식' AND (r.category ILIKE '%vegetarian%' OR r.category ILIKE '%vegan%'))
                        OR (:category = '디저트' AND (
                            r.category ILIKE '%dessert%' OR r.category ILIKE '%cafe%' OR r.category ILIKE '%bakery%'
                            OR trim(upper(r.category)) IN ('CE7')
                            OR r.description ILIKE '%카페%'))
                    )
                    AND (:minRating IS NULL OR r.rating >= :minRating)
                    AND (
                        :minLat IS NULL
                        OR ST_Contains(
                            ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326),
                            ST_SetSRID(ST_MakePoint(r.longitude, r.latitude), 4326)
                        )
                    )
                    """,
            nativeQuery = true)
    Page<Restaurant> searchOrderByDistance(
            @Param("category") String category,
            @Param("minRating") Double minRating,
            @Param("minLat") Double minLat,
            @Param("minLng") Double minLng,
            @Param("maxLat") Double maxLat,
            @Param("maxLng") Double maxLng,
            @Param("centerLat") Double centerLat,
            @Param("centerLng") Double centerLng,
            Pageable pageable);

    @Query(
            value =
                    """
                    SELECT r.* FROM restaurants r
                    WHERE r.id IN (:ids)
                    AND (
                        :category IS NULL
                        OR r.category = :category
                        OR r.category ILIKE CONCAT('%', :category, '%')
                        OR r.description ILIKE CONCAT('%', :category, '%')
                        OR (:category = '한식' AND r.category ILIKE '%korean%')
                        OR (:category = '일식' AND r.category ILIKE '%japanese%')
                        OR (:category = '중식' AND r.category ILIKE '%chinese%')
                        OR (:category = '양식' AND (r.category ILIKE '%western%' OR r.category ILIKE '%italian%' OR r.category ILIKE '%french%'))
                        OR (:category = '채식' AND (r.category ILIKE '%vegetarian%' OR r.category ILIKE '%vegan%'))
                        OR (:category = '디저트' AND (
                            r.category ILIKE '%dessert%' OR r.category ILIKE '%cafe%' OR r.category ILIKE '%bakery%'
                            OR trim(upper(r.category)) IN ('CE7')
                            OR r.description ILIKE '%카페%'))
                    )
                    AND (:minRating IS NULL OR r.rating >= :minRating)
                    AND (
                        :minLat IS NULL
                        OR ST_Contains(
                            ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326),
                            ST_SetSRID(ST_MakePoint(r.longitude, r.latitude), 4326)
                        )
                    )
                    ORDER BY
                        (ABS(COALESCE(r.latitude, :centerLat) - :centerLat) + ABS(COALESCE(r.longitude, :centerLng) - :centerLng)) ASC,
                        r.id ASC
                    """,
            countQuery =
                    """
                    SELECT count(*) FROM restaurants r
                    WHERE r.id IN (:ids)
                    AND (
                        :category IS NULL
                        OR r.category = :category
                        OR r.category ILIKE CONCAT('%', :category, '%')
                        OR r.description ILIKE CONCAT('%', :category, '%')
                        OR (:category = '한식' AND r.category ILIKE '%korean%')
                        OR (:category = '일식' AND r.category ILIKE '%japanese%')
                        OR (:category = '중식' AND r.category ILIKE '%chinese%')
                        OR (:category = '양식' AND (r.category ILIKE '%western%' OR r.category ILIKE '%italian%' OR r.category ILIKE '%french%'))
                        OR (:category = '채식' AND (r.category ILIKE '%vegetarian%' OR r.category ILIKE '%vegan%'))
                        OR (:category = '디저트' AND (
                            r.category ILIKE '%dessert%' OR r.category ILIKE '%cafe%' OR r.category ILIKE '%bakery%'
                            OR trim(upper(r.category)) IN ('CE7')
                            OR r.description ILIKE '%카페%'))
                    )
                    AND (:minRating IS NULL OR r.rating >= :minRating)
                    AND (
                        :minLat IS NULL
                        OR ST_Contains(
                            ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326),
                            ST_SetSRID(ST_MakePoint(r.longitude, r.latitude), 4326)
                        )
                    )
                    """,
            nativeQuery = true)
    Page<Restaurant> searchAmongIdsOrderByDistance(
            @Param("ids") java.util.Collection<Long> ids,
            @Param("category") String category,
            @Param("minRating") Double minRating,
            @Param("minLat") Double minLat,
            @Param("minLng") Double minLng,
            @Param("maxLat") Double maxLat,
            @Param("maxLng") Double maxLng,
            @Param("centerLat") Double centerLat,
            @Param("centerLng") Double centerLng,
            Pageable pageable);
}
