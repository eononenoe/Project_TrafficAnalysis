package com.example.traffic.service;

import com.example.traffic.dto.TrafficStatsDto;
import com.example.traffic.repository.TrafficRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TrafficService {
    private final TrafficRepository trafficRepository;

    public  List<TrafficStatsDto> getStats(String cctvId) {
        List<Object[]> results = trafficRepository.findAvgCountByCctv(cctvId);
        List<TrafficStatsDto> dtoList = new ArrayList<>();

        for(Object[] row : results){
            String weekday = (String) row[0];
            String hour = (String) row[1];
            Double avgCount = ((Number)row[2]).doubleValue();

            //혼잡도 계산 로직
            String congestion = "";
            if(avgCount > 50){
                congestion = "HIGH";
            } else if (avgCount > 20) {
                congestion = "MID";
            } else {
                congestion = "LOW";
            }
            dtoList.add(new TrafficStatsDto(weekday, hour, avgCount, congestion));
        }
        return dtoList;
    }
}
