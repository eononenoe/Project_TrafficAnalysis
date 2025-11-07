// AnalysisStatusDto.java (분석 상태)
package com.example.traffic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisStatusDto {
    private String cctvId;
    private String status; // idle, starting, running, completed, error, stopped
    private String message;
    private int currentVehicleCount;
    private LocalDateTime lastUpdated;
}