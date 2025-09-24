package com.example.traffic.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "TRAFFIC_DATA")
@Getter
@Setter
public class TrafficEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CCTV_ID", nullable = false)
    private String cctvId;

    @Column(name = "TIMESTAMP", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "VEHICLE_COUNT", nullable = false)
    private int vehicleCount;
}
