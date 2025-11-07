package com.example.traffic.repository;

import com.example.traffic.entity.CctvInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CctvRepository extends JpaRepository<CctvInfo, Long> {

    /**
     * 노선명으로 CCTV 조회
     */
    List<CctvInfo> findByLineName(String lineName);

    /**
     * 위치명으로 CCTV 검색 (부분 일치)
     */
    List<CctvInfo> findByLocationNameContaining(String keyword);

    /**
     * 노선명과 위치명으로 CCTV 조회
     */
    List<CctvInfo> findByLineNameAndLocationName(String lineName, String locationName);

    /**
     * 상태별 CCTV 조회
     */
    List<CctvInfo> findByStatus(String status);

    /**
     * 좌표 범위 내 CCTV 조회
     */
    @Query("SELECT c FROM CctvInfo c WHERE " +
            "c.coordX BETWEEN :minX AND :maxX AND " +
            "c.coordY BETWEEN :minY AND :maxY")
    List<CctvInfo> findByCoordinateRange(
            @Param("minX") Double minX,
            @Param("maxX") Double maxX,
            @Param("minY") Double minY,
            @Param("maxY") Double maxY
    );

    /**
     * 위도/경도 기반 근처 CCTV 조회 (지도용)
     */
    @Query("SELECT c FROM CctvInfo c WHERE " +
            "c.lat BETWEEN :minLat AND :maxLat AND " +
            "c.lng BETWEEN :minLng AND :maxLng")
    List<CctvInfo> findByLatLngRange(
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLng") Double minLng,
            @Param("maxLng") Double maxLng
    );

    /**
     * 노선별 CCTV 개수 집계
     */
    @Query("SELECT c.lineName, COUNT(c) FROM CctvInfo c " +
            "GROUP BY c.lineName " +
            "ORDER BY COUNT(c) DESC")
    List<Object[]> countByLineName();

    /**
     * 상태별 CCTV 개수 집계
     */
    @Query("SELECT c.status, COUNT(c) FROM CctvInfo c " +
            "GROUP BY c.status")
    List<Object[]> countByStatus();

    /**
     * 활성화된 CCTV만 조회
     */
    @Query("SELECT c FROM CctvInfo c WHERE c.status = 'ACTIVE'")
    List<CctvInfo> findAllActive();
}