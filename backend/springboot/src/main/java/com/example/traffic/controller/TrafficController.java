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

    @PostMapping("/start")
    public String startAnalysis(@RequestParam String cctvId,
                                @RequestParam double minX,
                                @RequestParam double maxX,
                                @RequestParam double minY,
                                @RequestParam double maxY) {
        try {
            //  Python 실행 파일 절대 경로 (Python 3.10)
            String pythonPath = "C:\\Users\\User\\AppData\\Local\\Programs\\Python\\Python310\\python.exe";

            //  main_pipeline.py 절대 경로
            String scriptPath = "C:\\Users\\User\\Desktop\\Project_TrafficAnalysis\\backend\\yolov7\\main_pipeline.py";

            //  실행 명령 구성
            String command = String.format(
                    "\"%s\" \"%s\" --minX %f --maxX %f --minY %f --maxY %f",
                    pythonPath, scriptPath, minX, maxX, minY, maxY
            );

            System.out.println("[SpringBoot] Python 실행: " + command);

            //  Runtime 실행
            Process process = Runtime.getRuntime().exec(command);

            //  Python 표준 출력 로그
            new Thread(() -> {
                try (var reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[Python STDOUT] " + line);
                    }
                } catch (Exception ignored) {}
            }).start();

            //  Python 에러 로그
            new Thread(() -> {
                try (var reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println("[Python STDERR] " + line);
                    }
                } catch (Exception ignored) {}
            }).start();

            return "OK - Python main_pipeline 실행됨";

        } catch (Exception e) {
            e.printStackTrace();
            return "FAIL - Python 실행 실패: " + e.getMessage();
        }
    }


}
