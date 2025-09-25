package com.example.traffic.repository;

import com.example.traffic.entity.TrafficEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrafficRepository extends JpaRepository<TrafficEntity, Long> {
        @Query( value = """
            SELECT 
                TO_CHAR(t.timestamp, 'DY', 'NLS_DATE_LANGUAGE=AMERICAN') AS weekday,
                TO_CHAR(t.timestamp, 'HH24') AS hour,
                ROUND(AVG(t.vehicle_count), 2) AS avg_count
            FROM TRAFFIC_DATA t
            WHERE t.cctv_id = :cctvId
            GROUP BY TO_CHAR(t.timestamp, 'DY', 'NLS_DATE_LANGUAGE=AMERICAN'),
                     TO_CHAR(t.timestamp, 'HH24')
            ORDER BY weekday, hour
            """, nativeQuery = true)
    List<Object[]> findAvgCountByCctv(@Param("cctvId")String cctvId);
}
