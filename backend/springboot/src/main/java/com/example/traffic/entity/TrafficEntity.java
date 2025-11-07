// ========== TrafficEntity.java (개선) ==========
package com.example.traffic.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "TRAFFIC_DATA", indexes = {
        @Index(name = "idx_cctv_timestamp", columnList = "CCTV_ID, TIMESTAMP"),
        @Index(name = "idx_timestamp", columnList = "TIMESTAMP")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrafficEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CCTV_ID", nullable = false, length = 50)
    private String cctvId;

    @Column(name = "TIMESTAMP", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "VEHICLE_COUNT", nullable = false)
    private int vehicleCount;

    // 혼잡도 레벨 (LOW, MID, HIGH) - 저장 시점에 계산
    @Column(name = "CONGESTION_LEVEL", length = 10)
    private String congestionLevel;

    // 평균 속도 (선택 사항)
    @Column(name = "AVG_SPEED")
    private Double avgSpeed;

    // 데이터 수집 주기 (초 단위)
    @Column(name = "COLLECTION_INTERVAL")
    private Integer collectionInterval = 5;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        // 혼잡도 자동 계산
        calculateCongestionLevel();
    }

    private void calculateCongestionLevel() {
        if (vehicleCount > 50) {
            this.congestionLevel = "HIGH";
        } else if (vehicleCount > 20) {
            this.congestionLevel = "MID";
        } else {
            this.congestionLevel = "LOW";
        }
    }
}