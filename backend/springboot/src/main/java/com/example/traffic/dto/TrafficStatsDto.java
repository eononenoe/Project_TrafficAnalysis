package com.example.traffic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrafficStatsDto {
    private String weekday;
    private String hour;
    private Double avgCount;
    private String congestion;  // LOW / MID / HIGH
}