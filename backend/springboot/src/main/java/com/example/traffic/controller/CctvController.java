package com.example.traffic.controller;

import org.springframework.web.bind.annotation.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/cctv")
public class CctvController {

    @GetMapping("/url")
    public Map<String, String> getCctvUrl(
            @RequestParam double minX,
            @RequestParam double maxX,
            @RequestParam double minY,
            @RequestParam double maxY
    ) {
        try {
            // Python 스크립트 실행 (traffic_counter.py의 get_cctv 부분만 호출)
            String pythonExe = "C:/Users/User/Desktop/Project_TrafficAnalysis/backend/yolov7/.venv/Scripts/python.exe";
            String scriptPath = "C:/Users/User/Desktop/Project_TrafficAnalysis/backend/yolov7/get_cctv_url.py";

            ProcessBuilder pb = new ProcessBuilder(
                    pythonExe, scriptPath,
                    "--minX", String.valueOf(minX),
                    "--maxX", String.valueOf(maxX),
                    "--minY", String.valueOf(minY),
                    "--maxY", String.valueOf(maxY)
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Python에서 CCTV URL 출력 받기
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String cctvUrl = null;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Python] " + line);
                if (line.startsWith("http")) { // URL 출력 시 잡기
                    cctvUrl = line.trim();
                }
            }
            process.waitFor();

            if (cctvUrl != null) {
                return Collections.singletonMap("cctvUrl", cctvUrl);
            } else {
                return Collections.singletonMap("error", "CCTV URL not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.singletonMap("error", e.getMessage());
        }
    }
}
