package com.example.traffic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrafficStatsDto {
    private String weekday;
    private String hour;
    private Double avgCount;
    private String congestion;  // LOW / MID / HIGH
    private LocalDateTime timestamp; // 추가

    // 기존 생성자 유지 (하위 호환성)
    public TrafficStatsDto(String weekday, String hour, Double avgCount, String congestion) {
        this.weekday = weekday;
        this.hour = hour;
        this.avgCount = avgCount;
        this.congestion = congestion;
        this.timestamp = LocalDateTime.now();
    }
}