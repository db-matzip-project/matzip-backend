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
                    WHERE (:category IS NULL OR r.category = :category)
                    AND (:minRating IS NULL OR r.rating >= :minRating)
                    AND (
                        :minLat IS NULL
                        OR ST_Contains(
                            ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326),
                            ST_SetSRID(ST_MakePoint(r.longitude, r.latitude), 4326)
                        )
                    )
                    """,
            countQuery =
                    """
                    SELECT count(*) FROM restaurants r
                    WHERE (:category IS NULL OR r.category = :category)
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
            Pageable pageable);

    /** 입맛 비슷한 사용자 추천 식당 ID 집합과 bounds 검색 교집합 */
    @Query(
            value =
                    """
                    SELECT r.* FROM restaurants r
                    WHERE r.id IN (:ids)
                    AND (:category IS NULL OR r.category = :category)
                    AND (:minRating IS NULL OR r.rating >= :minRating)
                    AND (
                        :minLat IS NULL
                        OR ST_Contains(
                            ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326),
                            ST_SetSRID(ST_MakePoint(r.longitude, r.latitude), 4326)
                        )
                    )
                    """,
            countQuery =
                    """
                    SELECT count(*) FROM restaurants r
                    WHERE r.id IN (:ids)
                    AND (:category IS NULL OR r.category = :category)
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
            Pageable pageable);
}
