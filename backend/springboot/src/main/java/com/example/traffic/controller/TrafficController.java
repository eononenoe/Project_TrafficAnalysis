package com.example.traffic.controller;

import com.example.traffic.dto.TrafficData;
import com.example.traffic.entity.TrafficEntity;
import com.example.traffic.repository.TrafficRepository;
import com.example.traffic.service.TrafficService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.traffic.dto.TrafficStatsDto;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/traffic")
public class TrafficController {

    private final TrafficService trafficService;

    @Autowired
    private TrafficRepository trafficRepository;

    @PostMapping("/save")
    public String saveTraffic(@RequestBody TrafficData data) {
        System.out.println("=== 차량 데이터 수신 ===");
        System.out.println("CCTV: " + data.getCctvId());
        System.out.println("시간: " + data.getTimestamp());
        System.out.println("차량 수: " + data.getVehicleCount());

        // DTO -> Entity 변환
        TrafficEntity entity = new TrafficEntity();
        entity.setCctvId(data.getCctvId());
        entity.setVehicleCount(data.getVehicleCount());
        entity.setTimestamp(data.getTimestamp());

        trafficRepository.save(entity);  // DB 저장
        return "OK";
    }

    @GetMapping("/stats")
    public List<TrafficStatsDto> getTrafficStats(@RequestParam String cctvId) {
        return trafficService.getStats(cctvId);
    }
}
