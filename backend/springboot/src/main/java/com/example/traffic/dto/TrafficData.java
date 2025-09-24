package com.example.traffic.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class TrafficData {
    private String cctvId;
    private LocalDateTime timestamp;
    private int vehicleCount;
}
