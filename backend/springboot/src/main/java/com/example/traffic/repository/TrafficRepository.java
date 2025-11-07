package com.example.traffic.repository;

import com.example.traffic.entity.TrafficEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrafficRepository extends JpaRepository<TrafficEntity, Long> {

    /**
     * 요일/시간대별 평균 차량 수 (기존)
     */
    @Query(value = """
            SELECT 
                TO_CHAR(t.timestamp, 'DY', 'NLS_DATE_LANGUAGE=AMERICAN') AS weekday,
                TO_CHAR(t.timestamp, 'HH24') AS hour,
                ROUND(AVG(t.vehicle_count), 2) AS avg_count,
                t.timestamp
            FROM TRAFFIC_DATA t
            WHERE t.cctv_id = :cctvId
            GROUP BY TO_CHAR(t.timestamp, 'DY', 'NLS_DATE_LANGUAGE=AMERICAN'),
                     TO_CHAR(t.timestamp, 'HH24'),
                     t.timestamp
            ORDER BY t.timestamp DESC
            """, nativeQuery = true)
    List<Object[]> findAvgCountByCctv(@Param("cctvId") String cctvId);

    /**
     * 최근 데이터 조회 (실시간 혼잡도용)
     */
    TrafficEntity findTopByCctvIdOrderByTimestampDesc(String cctvId);

    /**
     * 특정 기간의 데이터 조회
     */
    @Query("SELECT t FROM TrafficEntity t WHERE " +
            "t.cctvId = :cctvId AND " +
            "t.timestamp BETWEEN :startTime AND :endTime " +
            "ORDER BY t.timestamp DESC")
    List<TrafficEntity> findByCctvIdAndTimeRange(
            @Param("cctvId") String cctvId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 시간대별 평균 차량 수 (최근 24시간)
     */
    @Query(value = """
            SELECT 
                TO_CHAR(t.timestamp, 'HH24') AS hour,
                ROUND(AVG(t.vehicle_count), 2) AS avg_count,
                MAX(t.timestamp) AS latest_time
            FROM TRAFFIC_DATA t
            WHERE t.cctv_id = :cctvId
              AND t.timestamp >= SYSDATE - 1
            GROUP BY TO_CHAR(t.timestamp, 'HH24')
            ORDER BY hour
            """, nativeQuery = true)
    List<Object[]> findHourlyAverageLast24Hours(@Param("cctvId") String cctvId);

    /**
     * 혼잡도별 데이터 개수
     */
    @Query("SELECT t.congestionLevel, COUNT(t) FROM TrafficEntity t " +
            "WHERE t.cctvId = :cctvId " +
            "GROUP BY t.congestionLevel")
    List<Object[]> countByCongestionLevel(@Param("cctvId") String cctvId);

    /**
     * 최근 N개 데이터 조회
     */
    @Query("SELECT t FROM TrafficEntity t WHERE t.cctvId = :cctvId " +
            "ORDER BY t.timestamp DESC")
    List<TrafficEntity> findRecentByCctvId(@Param("cctvId") String cctvId);

    /**
     * 특정 시간대의 평균 차량 수
     */
    @Query("SELECT AVG(t.vehicleCount) FROM TrafficEntity t WHERE " +
            "t.cctvId = :cctvId AND " +
            "HOUR(t.timestamp) = :hour")
    Double findAvgCountByHour(
            @Param("cctvId") String cctvId,
            @Param("hour") int hour
    );

    /**
     * 오늘의 최대/최소 차량 수
     */
    @Query(value = """
            SELECT 
                MAX(t.vehicle_count) AS max_count,
                MIN(t.vehicle_count) AS min_count,
                AVG(t.vehicle_count) AS avg_count
            FROM TRAFFIC_DATA t
            WHERE t.cctv_id = :cctvId
              AND TRUNC(t.timestamp) = TRUNC(SYSDATE)
            """, nativeQuery = true)
    Object[] findTodayStats(@Param("cctvId") String cctvId);

    /**
     * 데이터 삭제 (오래된 데이터 정리용)
     */
    @Query("DELETE FROM TrafficEntity t WHERE t.timestamp < :cutoffDate")
    void deleteOldData(@Param("cutoffDate") LocalDateTime cutoffDate);
}