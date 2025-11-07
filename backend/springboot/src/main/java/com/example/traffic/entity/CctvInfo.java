// ========== CctvInfo.java (개선) ==========
package com.example.traffic.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "CCTV_INFO", indexes = {
        @Index(name = "idx_line_name", columnList = "LINE_NAME"),
        @Index(name = "idx_location_name", columnList = "LOCATION_NAME")
})
public class CctvInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "COORD_X")
    private Double coordX;

    @Column(name = "COORD_Y")
    private Double coordY;

    @Column(name = "MIN_X")
    private Double minX;

    @Column(name = "MAX_X")
    private Double maxX;

    @Column(name = "MIN_Y")
    private Double minY;

    @Column(name = "MAX_Y")
    private Double maxY;

    @Column(name = "STREAM_URL", length = 500)
    private String streamUrl;

    @Column(name = "LINE_NAME", length = 100)
    private String lineName;

    @Column(name = "LOCATION_NAME", length = 200)
    private String locationName;

    // 지도 표시용 위도/경도 (추가)
    @Column(name = "LAT")
    private Double lat;

    @Column(name = "LNG")
    private Double lng;

    // CCTV 상태 (ACTIVE, INACTIVE, MAINTENANCE)
    @Column(name = "STATUS", length = 20)
    private String status = "ACTIVE";

    // 등록 시간
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    // 마지막 업데이트 시간
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
